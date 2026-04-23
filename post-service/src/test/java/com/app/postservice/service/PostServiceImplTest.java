package com.app.postservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.app.postservice.dto.PostCreateRequest;
import com.app.postservice.dto.PostResponse;
import com.app.postservice.dto.PostUpdateRequest;
import com.app.postservice.entity.AuthorFollow;
import com.app.postservice.entity.Post;
import com.app.postservice.entity.PostLike;
import com.app.postservice.entity.PostStatus;
import com.app.postservice.messaging.NotificationDispatchEvent;
import com.app.postservice.repository.AuthorFollowRepository;
import com.app.postservice.repository.PostLikeRepository;
import com.app.postservice.repository.PostRepository;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private AuthorFollowRepository authorFollowRepository;

    @Mock
    @SuppressWarnings("unused")
    private ObjectProvider<StringRedisTemplate> redisTemplateProvider;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectProvider<KafkaTemplate<String, NotificationDispatchEvent>> notificationKafkaTemplateProvider;

    @InjectMocks
    private PostServiceImpl postService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(postService, "newsletterServiceUrl", "http://newsletter");
        ReflectionTestUtils.setField(postService, "commentServiceUrl", "http://comment");
        ReflectionTestUtils.setField(postService, "authServiceUrl", "http://auth");
        ReflectionTestUtils.setField(postService, "frontendUrl", "http://frontend");
        ReflectionTestUtils.setField(postService, "internalApiKey", "secret");
        ReflectionTestUtils.setField(postService, "freeAuthorPostLimit", 5);
        ReflectionTestUtils.setField(postService, "viewRedisPrefix", "post:views:session");
        ReflectionTestUtils.setField(postService, "viewSessionTtlHours", 24L);
        ReflectionTestUtils.setField(postService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(postService, "notificationKafkaTopic", "notification.dispatch.v1");
        ReflectionTestUtils.setField(postService, "applicationName", "post-service");
        ReflectionTestUtils.setField(postService, "redisTemplateProvider", redisTemplateProvider);
        ReflectionTestUtils.setField(postService, "notificationKafkaTemplateProvider", notificationKafkaTemplateProvider);
    }
    @Test
    void createPostBuildsResponseAndPersistsDraft() {
        PostCreateRequest request = new PostCreateRequest();
        request.setAuthorId(10L);
        request.setAuthorName("Jane Doe");
        request.setTitle("Hello World");
        request.setContent("This is a post body with enough words to calculate time.");
        request.setExcerpt("");
        when(postRepository.existsBySlug("hello-world")).thenReturn(false);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setPostId(100L);
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());
            return post;
        });

        PostResponse response = postService.createPost(request, "ADMIN");

        assertThat(response.getPostId()).isEqualTo(100L);
        assertThat(response.getSlug()).isEqualTo("hello-world");
        assertThat(response.getStatus()).isEqualTo(PostStatus.DRAFT);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void likeAndUnlikePostUpdateCountersOncePerUser() {
        Post post = basePost(1L);
        when(postRepository.findByPostId(1L)).thenReturn(java.util.Optional.of(post));
        AtomicInteger lookupCount = new AtomicInteger();
        when(postLikeRepository.findByPostIdAndUserId(1L, 9L)).thenAnswer(invocation ->
                lookupCount.getAndIncrement() == 0
                        ? Optional.empty()
                        : Optional.of(PostLike.builder().postId(1L).userId(9L).build()));
        when(postLikeRepository.save(any(PostLike.class))).thenAnswer(invocation -> invocation.getArgument(0));

        postService.likePost(1L, 9L);
        postService.unlikePost(1L, 9L);

        verify(postRepository).incrementLikes(1L);
        verify(postRepository).decrementLikes(1L);
    }

    @Test
    void followAuthorPreventsDuplicateAndSupportsUnfollow() {
        AtomicInteger lookupCount = new AtomicInteger();
        when(authorFollowRepository.findByAuthorIdAndFollowerId(5L, 7L)).thenAnswer(invocation -> {
            int call = lookupCount.getAndIncrement();
            if (call == 0) {
                return Optional.empty();
            }
            if (call == 1) {
                return Optional.of(AuthorFollow.builder().authorId(5L).followerId(7L).build());
            }
            return Optional.empty();
        });
        when(authorFollowRepository.save(any(AuthorFollow.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authorFollowRepository.countByAuthorId(5L)).thenReturn(1L);

        assertThat(postService.followAuthor(5L, 7L)).isTrue();
        assertThat(postService.unfollowAuthor(5L, 7L)).isTrue();
        assertThat(postService.isFollowingAuthor(5L, 7L)).isFalse();
        assertThat(postService.getAuthorFollowerCount(5L)).isEqualTo(1L);
    }

    @Test
    void incrementViewsOnlyCountsFirstViewForSession() {
        when(postRepository.findByPostId(1L)).thenReturn(java.util.Optional.of(basePost(1L)));

        postService.incrementViews(1L, "session-1");
        postService.incrementViews(1L, "session-1");

        verify(postRepository).incrementViews(1L);
        verify(postRepository, never()).delete(any());
    }

    @Test
    void getPublishedPostsByAuthorUsesRepositoryMapping() {
        when(postRepository.findByAuthorIdAndStatusOrderByPublishedAtDesc(2L, PostStatus.PUBLISHED))
                .thenReturn(List.of(basePost(2L)));

        assertThat(postService.getPublishedPostsByAuthor(2L)).hasSize(1);
    }

    private Post basePost(Long postId) {
        return Post.builder()
                .postId(postId)
                .authorId(10L)
                .authorName("Author")
                .title("Hello World")
                .slug("hello-world")
                .content("content")
                .excerpt("excerpt")
                .featuredImageUrl("img")
                .status(PostStatus.DRAFT)
                .readTimeMin(1)
                .viewCount(0L)
                .likesCount(0L)
                .featured(false)
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now().minusMinutes(1))
                .build();
    }

    @Test
    void getAndCountOperationsUseRepositories() {
        Post post = basePost(9L);
        when(postRepository.findByPostId(9L)).thenReturn(java.util.Optional.of(post));
        when(postRepository.findBySlug("hello-world")).thenReturn(java.util.Optional.of(post));
        when(postRepository.findByAuthorIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(post));
        when(postRepository.findAllByOrderByUpdatedAtDesc()).thenReturn(List.of(post));
        when(postRepository.findPublishedOrderByPublishedAtDesc()).thenReturn(List.of(post));
        when(postRepository.findAllByOrderByViewCountDescPublishedAtDesc()).thenReturn(List.of(post));
        when(postRepository.searchByTitle("hello")).thenReturn(List.of(post));
        when(postRepository.count()).thenReturn(8L);
        when(postRepository.countByAuthorId(10L)).thenReturn(3L);
        when(authorFollowRepository.findByFollowerId(7L))
                .thenReturn(List.of(AuthorFollow.builder().authorId(10L).followerId(7L).build()));

        assertThat(postService.getPostById(9L).getPostId()).isEqualTo(9L);
        assertThat(postService.getPostBySlug("hello-world").getSlug()).isEqualTo("hello-world");
        assertThat(postService.getPostsByAuthor(10L)).hasSize(1);
        assertThat(postService.getAllPosts()).hasSize(1);
        assertThat(postService.getPublishedPosts()).hasSize(1);
        assertThat(postService.getMostViewedPosts(3)).hasSize(1);
        assertThat(postService.searchPosts("hello")).hasSize(1);
        assertThat(postService.getPostCount(null)).isEqualTo(8L);
        assertThat(postService.getPostCount(10L)).isEqualTo(3L);
        assertThat(postService.getFollowedAuthorIds(7L)).containsExactly(10L);
    }

    @Test
    void updatePublishUnpublishFeatureAndDeleteFlow() {
        Post post = basePost(5L);
        post.setPublishedAt(LocalDateTime.now().minusDays(1));
        when(postRepository.findByPostId(5L)).thenReturn(java.util.Optional.of(post));
        when(postRepository.existsBySlug("updated-title")).thenReturn(false);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PostUpdateRequest updateRequest = new PostUpdateRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setContent("Updated content body");
        updateRequest.setExcerpt("Updated excerpt");
        updateRequest.setFeaturedImageUrl("img-2");
        updateRequest.setStatus(PostStatus.DRAFT);

        assertThat(postService.updatePost(5L, updateRequest).getTitle()).isEqualTo("Updated Title");
        assertThat(postService.publishPost(5L).getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(postService.unpublishPost(5L).getStatus()).isEqualTo(PostStatus.UNPUBLISHED);
        assertThat(postService.featurePost(5L, true).getFeatured()).isTrue();

        postService.deletePost(5L);

        verify(postLikeRepository).deleteByPostId(5L);
        verify(postRepository).delete(post);
    }

    @Test
    void incrementViewsRequiresSessionId() {
        assertThat(org.assertj.core.api.Assertions.catchThrowable(() -> postService.incrementViews(1L, " ")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validationAndLimitBranchesAreCovered() {
        assertThatThrownBy(() -> postService.followAuthor(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot follow yourself");
        assertThatThrownBy(() -> postService.likePost(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId is required");

        when(postRepository.findByPostId(2L)).thenReturn(java.util.Optional.of(basePost(2L)));
        when(postLikeRepository.findByPostIdAndUserId(2L, 20L)).thenReturn(java.util.Optional.empty());
        postService.unlikePost(2L, 20L);

        when(postRepository.findAllByOrderByViewCountDescPublishedAtDesc())
                .thenReturn(List.of(basePost(1L), basePost(2L), basePost(3L)));
        assertThat(postService.getMostViewedPosts(500)).hasSize(3);
    }

    @Test
    void helperMethodsAndQuotaFailuresAreCovered() {
        when(postRepository.existsBySlug("post")).thenReturn(true);
        when(postRepository.existsBySlug("post-2")).thenReturn(false);
        assertThat((String) ReflectionTestUtils.invokeMethod(postService, "generateUniqueSlug", "   "))
                .isEqualTo("post-2");

        assertThat((Integer) ReflectionTestUtils.invokeMethod(postService, "computeReadTimeMinutes", "<p></p>"))
                .isEqualTo(1);
        assertThat((String) ReflectionTestUtils.invokeMethod(postService, "resolveExcerpt", "x".repeat(450), "content"))
                .hasSize(400);
        assertThat((String) ReflectionTestUtils.invokeMethod(postService, "trimTrailingSlash", "http://frontend/"))
                .isEqualTo("http://frontend");

        PostCreateRequest request = new PostCreateRequest();
        request.setAuthorId(50L);
        request.setTitle("Title");
        request.setContent("content");
        request.setAuthorName("Author");

        when(restTemplate.exchange(any(String.class), any(), any(), any(Class.class)))
                .thenReturn((ResponseEntity) ResponseEntity.ok(null));

        assertThatThrownBy(() -> postService.createPost(request, "AUTHOR"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entitlement");
    }

    @Test
    void internalStatusAndSessionHelpersCoverExtraBranches() {
        Post post = basePost(15L);
        post.setPublishedAt(null);
        ReflectionTestUtils.invokeMethod(postService, "applyStatus", post, PostStatus.PUBLISHED);
        assertThat(post)
                .extracting(Post::getStatus, value -> value.getPublishedAt() != null)
                .containsExactly(PostStatus.PUBLISHED, true);

        ReflectionTestUtils.invokeMethod(postService, "applyStatus", post, PostStatus.UNPUBLISHED);
        assertThat(post.getFeatured()).isFalse();

        assertThat((String) ReflectionTestUtils.invokeMethod(postService, "resolveAuthorName", " ", 7L))
                .isEqualTo("User #7");
        assertThat((Boolean) ReflectionTestUtils.invokeMethod(postService, "registerViewInMemory", 9L, "session-a"))
                .isTrue();
        assertThat((Boolean) ReflectionTestUtils.invokeMethod(postService, "registerViewInMemory", 9L, "session-a"))
                .isFalse();
    }

    @Test
    void incrementViewsUsesRedisDeduplicationWhenAvailable() {
        StringRedisTemplate redisTemplate = org.mockito.Mockito.mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        SetOperations<String, String> setOperations = org.mockito.Mockito.mock(SetOperations.class);
        when(redisTemplateProvider.getIfAvailable()).thenReturn(redisTemplate);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.add(anyString(), anyString())).thenReturn(1L, 0L);
        when(postRepository.findByPostId(21L)).thenReturn(java.util.Optional.of(basePost(21L)));

        postService.incrementViews(21L, "session-redis");
        postService.incrementViews(21L, "session-redis");

        verify(setOperations, times(2)).add("post:views:session:session-redis", "21");
        verify(redisTemplate, times(2)).expire(anyString(), any(java.time.Duration.class));
        verify(postRepository, times(1)).incrementViews(21L);
    }

    @Test
    void createPublishedPostNotifiesSubscribersAndFollowers() {
        PostCreateRequest request = new PostCreateRequest();
        request.setAuthorId(11L);
        request.setAuthorName("Author");
        request.setTitle("New Post");
        request.setContent("content");
        request.setStatus(PostStatus.PUBLISHED);

        when(postRepository.existsBySlug("new-post")).thenReturn(false);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setPostId(200L);
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());
            return post;
        });
        when(authorFollowRepository.findByAuthorId(11L))
                .thenReturn(List.of(AuthorFollow.builder().authorId(11L).followerId(22L).build()));
        when(restTemplate.postForEntity(contains("/newsletter/send-post-notification"), any(), any(Class.class)))
                .thenReturn(ResponseEntity.ok().build());

        KafkaTemplate<String, NotificationDispatchEvent> kafkaTemplate = org.mockito.Mockito.mock(KafkaTemplate.class);
        when(notificationKafkaTemplateProvider.getIfAvailable()).thenReturn(kafkaTemplate);
        when(kafkaTemplate.send(anyString(), anyString(), any(NotificationDispatchEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        postService.createPost(request, "ADMIN");

        verify(restTemplate).postForEntity(contains("/newsletter/send-post-notification"), any(), any(Class.class));
        verify(kafkaTemplate, atLeast(2)).send(anyString(), anyString(), any(NotificationDispatchEvent.class));
    }
}
