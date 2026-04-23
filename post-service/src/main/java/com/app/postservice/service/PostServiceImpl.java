package com.app.postservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.postservice.dto.PostCreateRequest;
import com.app.postservice.dto.PostResponse;
import com.app.postservice.dto.PostUpdateRequest;
import com.app.postservice.entity.Post;
import com.app.postservice.entity.PostLike;
import com.app.postservice.entity.PostStatus;
import com.app.postservice.repository.PostLikeRepository;
import com.app.postservice.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");
    private static final Pattern TAGS = Pattern.compile("<[^>]*>");
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final Map<String, Set<Long>> viewedSessionPosts = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public PostResponse createPost(PostCreateRequest request) {
        PostStatus status = request.getStatus() == null ? PostStatus.DRAFT : request.getStatus();
        String slug = generateUniqueSlug(request.getTitle());
        Post post = Post.builder()
                .authorId(request.getAuthorId())
                .title(request.getTitle().trim())
                .slug(slug)
                .content(request.getContent())
                .excerpt(resolveExcerpt(request.getExcerpt(), request.getContent()))
                .featuredImageUrl(request.getFeaturedImageUrl())
                .status(status)
                .readTimeMin(computeReadTimeMinutes(request.getContent()))
                .publishedAt(status == PostStatus.PUBLISHED ? LocalDateTime.now() : null)
                .build();
        post = postRepository.save(post);
        return toResponse(post);
    }

    @Override
    public PostResponse getPostById(Long postId) {
        return toResponse(findPost(postId));
    }

    @Override
    public PostResponse getPostBySlug(String slug) {
        Post post = postRepository.findBySlug(slug).orElseThrow(() -> new IllegalArgumentException("Post not found"));
        return toResponse(post);
    }

    @Override
    public List<PostResponse> getPostsByAuthor(Long authorId) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(authorId).stream().map(this::toResponse).toList();
    }

    @Override
    public List<PostResponse> getPublishedPosts() {
        return postRepository.findPublishedOrderByPublishedAtDesc().stream().map(this::toResponse).toList();
    }

    @Override
    public List<PostResponse> searchPosts(String keyword) {
        return postRepository.searchByTitle(keyword == null ? "" : keyword).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest request) {
        Post post = findPost(postId);
        if (!post.getTitle().equalsIgnoreCase(request.getTitle().trim())) {
            post.setTitle(request.getTitle().trim());
            post.setSlug(generateUniqueSlug(request.getTitle()));
        }
        post.setContent(request.getContent());
        post.setExcerpt(resolveExcerpt(request.getExcerpt(), request.getContent()));
        post.setFeaturedImageUrl(request.getFeaturedImageUrl());
        post.setReadTimeMin(computeReadTimeMinutes(request.getContent()));
        if (request.getStatus() != null) {
            applyStatus(post, request.getStatus());
        }
        post = postRepository.save(post);
        return toResponse(post);
    }

    @Override
    @Transactional
    public PostResponse publishPost(Long postId) {
        Post post = findPost(postId);
        applyStatus(post, PostStatus.PUBLISHED);
        post = postRepository.save(post);
        return toResponse(post);
    }

    @Override
    @Transactional
    public PostResponse unpublishPost(Long postId) {
        Post post = findPost(postId);
        applyStatus(post, PostStatus.UNPUBLISHED);
        post = postRepository.save(post);
        return toResponse(post);
    }

    @Override
    @Transactional
    public void deletePost(Long postId) {
        Post post = findPost(postId);
        postLikeRepository.deleteByPostId(post.getPostId());
        postRepository.delete(post);
    }

    @Override
    @Transactional
    public void incrementViews(Long postId, String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId is required");
        }
        findPost(postId);
        Set<Long> viewed = viewedSessionPosts.computeIfAbsent(sessionId, id -> ConcurrentHashMap.newKeySet());
        if (viewed.contains(postId)) {
            return;
        }
        postRepository.incrementViews(postId);
        viewed.add(postId);
    }

    @Override
    @Transactional
    public void likePost(Long postId, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        findPost(postId);
        if (postLikeRepository.findByPostIdAndUserId(postId, userId).isPresent()) {
            return;
        }
        postLikeRepository.save(PostLike.builder().postId(postId).userId(userId).build());
        postRepository.incrementLikes(postId);
    }

    @Override
    @Transactional
    public void unlikePost(Long postId, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        findPost(postId);
        PostLike postLike = postLikeRepository.findByPostIdAndUserId(postId, userId).orElse(null);
        if (postLike == null) {
            return;
        }
        postLikeRepository.delete(postLike);
        postRepository.decrementLikes(postId);
    }

    @Override
    public long getPostCount(Long authorId) {
        if (authorId == null) {
            return postRepository.count();
        }
        return postRepository.countByAuthorId(authorId);
    }

    @Override
    @Transactional
    public PostResponse featurePost(Long postId, boolean featured) {
        Post post = findPost(postId);
        post.setFeatured(featured);
        post = postRepository.save(post);
        return toResponse(post);
    }

    private Post findPost(Long postId) {
        return postRepository.findByPostId(postId).orElseThrow(() -> new IllegalArgumentException("Post not found"));
    }

    private void applyStatus(Post post, PostStatus status) {
        post.setStatus(status);
        if (status == PostStatus.PUBLISHED && post.getPublishedAt() == null) {
            post.setPublishedAt(LocalDateTime.now());
        }
        if (status != PostStatus.PUBLISHED) {
            post.setFeatured(false);
        }
    }

    private String generateUniqueSlug(String title) {
        String base = NON_ALNUM.matcher(title.trim().toLowerCase()).replaceAll("-")
                .replaceAll("(^-+|-+$)", "");
        if (base.isBlank()) {
            base = "post";
        }
        String slug = base;
        int count = 2;
        while (postRepository.existsBySlug(slug)) {
            slug = base + "-" + count++;
        }
        return slug;
    }

    private Integer computeReadTimeMinutes(String content) {
        String text = TAGS.matcher(content).replaceAll(" ").trim();
        if (text.isEmpty()) {
            return 1;
        }
        String[] words = text.split("\\s+");
        return Math.max(1, (int) Math.ceil(words.length / 200.0));
    }

    private String resolveExcerpt(String excerpt, String content) {
        if (excerpt != null && !excerpt.isBlank()) {
            return excerpt.length() <= 400 ? excerpt : excerpt.substring(0, 400);
        }
        String text = TAGS.matcher(content).replaceAll(" ").replaceAll("\\s+", " ").trim();
        if (text.length() <= 200) {
            return text;
        }
        return text.substring(0, 200);
    }

    private PostResponse toResponse(Post post) {
        return PostResponse.builder()
                .postId(post.getPostId())
                .authorId(post.getAuthorId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .content(post.getContent())
                .excerpt(post.getExcerpt())
                .featuredImageUrl(post.getFeaturedImageUrl())
                .status(post.getStatus())
                .readTimeMin(post.getReadTimeMin())
                .viewCount(post.getViewCount())
                .likesCount(post.getLikesCount())
                .featured(post.getFeatured())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .publishedAt(post.getPublishedAt())
                .build();
    }
}
