package com.app.postservice.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.postservice.dto.PostCreateRequest;
import com.app.postservice.dto.PostResponse;
import com.app.postservice.dto.PostUpdateRequest;
import com.app.postservice.service.PostService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostResource {

    private final PostService postService;

    @PostMapping
    public PostResponse createPost(@Valid @RequestBody PostCreateRequest request) {
        return postService.createPost(request);
    }

    @GetMapping("/{postId}")
    public PostResponse getPostById(@PathVariable Long postId) {
        return postService.getPostById(postId);
    }

    @GetMapping("/slug/{slug}")
    public PostResponse getPostBySlug(@PathVariable String slug) {
        return postService.getPostBySlug(slug);
    }

    @GetMapping("/author/{authorId}")
    public List<PostResponse> getPostsByAuthor(@PathVariable Long authorId) {
        return postService.getPostsByAuthor(authorId);
    }

    @GetMapping("/published")
    public List<PostResponse> getPublishedPosts() {
        return postService.getPublishedPosts();
    }

    @GetMapping("/search")
    public List<PostResponse> searchPosts(@RequestParam(value = "q", defaultValue = "") String keyword) {
        return postService.searchPosts(keyword);
    }

    @PutMapping("/{postId}")
    public PostResponse updatePost(@PathVariable Long postId, @Valid @RequestBody PostUpdateRequest request) {
        return postService.updatePost(postId, request);
    }

    @PutMapping("/{postId}/publish")
    public PostResponse publishPost(@PathVariable Long postId) {
        return postService.publishPost(postId);
    }

    @PutMapping("/{postId}/unpublish")
    public PostResponse unpublishPost(@PathVariable Long postId) {
        return postService.unpublishPost(postId);
    }

    @PutMapping("/{postId}/feature")
    public PostResponse featurePost(@PathVariable Long postId, @RequestParam boolean featured) {
        return postService.featurePost(postId, featured);
    }

    @PostMapping("/{postId}/views")
    public Map<String, String> incrementViews(@PathVariable Long postId, @RequestParam String sessionId) {
        postService.incrementViews(postId, sessionId);
        return Map.of("message", "View counted");
    }

    @PostMapping("/{postId}/like")
    public Map<String, String> likePost(@PathVariable Long postId, @RequestParam Long userId) {
        postService.likePost(postId, userId);
        return Map.of("message", "Post liked");
    }

    @PostMapping("/{postId}/unlike")
    public Map<String, String> unlikePost(@PathVariable Long postId, @RequestParam Long userId) {
        postService.unlikePost(postId, userId);
        return Map.of("message", "Post unliked");
    }

    @DeleteMapping("/{postId}")
    public Map<String, String> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return Map.of("message", "Post deleted");
    }

    @GetMapping("/count")
    public Map<String, Long> getPostCount(@RequestParam(value = "authorId", required = false) Long authorId) {
        return Map.of("count", postService.getPostCount(authorId));
    }
}
