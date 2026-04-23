package com.app.commentservice.service;

import java.util.List;

import com.app.commentservice.dto.CommentCreateRequest;
import com.app.commentservice.dto.CommentResponse;
import com.app.commentservice.dto.CommentUpdateRequest;
import com.app.commentservice.dto.ModerationModeRequest;

public interface CommentService {
    CommentResponse addComment(CommentCreateRequest request);
    List<CommentResponse> getCommentsByPost(Long postId);
    CommentResponse getCommentById(Long commentId);
    List<CommentResponse> getReplies(Long commentId);
    CommentResponse updateComment(Long commentId, CommentUpdateRequest request);
    void deleteComment(Long commentId, Long actorId);
    CommentResponse approveComment(Long commentId);
    CommentResponse rejectComment(Long commentId);
    void likeComment(Long commentId, Long userId);
    void unlikeComment(Long commentId, Long userId);
    long getCommentCount(Long postId);
    boolean getModerationMode();
    void updateModerationMode(ModerationModeRequest request);
}
