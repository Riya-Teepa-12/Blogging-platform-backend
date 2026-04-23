package com.app.commentservice.controller;

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

import com.app.commentservice.dto.CommentCreateRequest;
import com.app.commentservice.dto.CommentResponse;
import com.app.commentservice.dto.CommentUpdateRequest;
import com.app.commentservice.dto.ModerationModeRequest;
import com.app.commentservice.service.CommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentResource {

    private final CommentService commentService;

    @PostMapping
    public CommentResponse addComment(@Valid @RequestBody CommentCreateRequest request) {
        return commentService.addComment(request);
    }

    @GetMapping("/post/{postId}")
    public List<CommentResponse> getCommentsByPost(@PathVariable Long postId) {
        return commentService.getCommentsByPost(postId);
    }

    @GetMapping("/{commentId}")
    public CommentResponse getCommentById(@PathVariable Long commentId) {
        return commentService.getCommentById(commentId);
    }

    @GetMapping("/{commentId}/replies")
    public List<CommentResponse> getReplies(@PathVariable Long commentId) {
        return commentService.getReplies(commentId);
    }

    @PutMapping("/{commentId}")
    public CommentResponse updateComment(@PathVariable Long commentId, @Valid @RequestBody CommentUpdateRequest request) {
        return commentService.updateComment(commentId, request);
    }

    @DeleteMapping("/{commentId}")
    public Map<String, String> deleteComment(@PathVariable Long commentId, @RequestParam Long actorId) {
        commentService.deleteComment(commentId, actorId);
        return Map.of("message", "Comment deleted");
    }

    @PutMapping("/{commentId}/approve")
    public CommentResponse approveComment(@PathVariable Long commentId) {
        return commentService.approveComment(commentId);
    }

    @PutMapping("/{commentId}/reject")
    public CommentResponse rejectComment(@PathVariable Long commentId) {
        return commentService.rejectComment(commentId);
    }

    @PutMapping("/{commentId}/like")
    public Map<String, String> likeComment(@PathVariable Long commentId, @RequestParam Long userId) {
        commentService.likeComment(commentId, userId);
        return Map.of("message", "Comment liked");
    }

    @PutMapping("/{commentId}/unlike")
    public Map<String, String> unlikeComment(@PathVariable Long commentId, @RequestParam Long userId) {
        commentService.unlikeComment(commentId, userId);
        return Map.of("message", "Comment unliked");
    }

    @GetMapping("/count")
    public Map<String, Long> getCommentCount(@RequestParam Long postId) {
        return Map.of("count", commentService.getCommentCount(postId));
    }

    @GetMapping("/moderation")
    public Map<String, Boolean> getModerationMode() {
        return Map.of("moderationRequired", commentService.getModerationMode());
    }

    @PutMapping("/moderation")
    public Map<String, Boolean> setModerationMode(@RequestBody ModerationModeRequest request) {
        commentService.updateModerationMode(request);
        return Map.of("moderationRequired", commentService.getModerationMode());
    }
}
