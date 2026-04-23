package com.app.commentservice.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.commentservice.dto.CommentCreateRequest;
import com.app.commentservice.dto.CommentResponse;
import com.app.commentservice.dto.CommentUpdateRequest;
import com.app.commentservice.dto.ModerationModeRequest;
import com.app.commentservice.entity.Comment;
import com.app.commentservice.entity.CommentLike;
import com.app.commentservice.entity.CommentStatus;
import com.app.commentservice.repository.CommentLikeRepository;
import com.app.commentservice.repository.CommentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;

    @Value("${inkwell.comment.moderation-required:false}")
    private boolean moderationRequired;

    private volatile boolean moderationModeInitialized;
    private volatile boolean moderationMode;

    @Override
    @Transactional
    public CommentResponse addComment(CommentCreateRequest request) {
        initializeModerationModeIfNeeded();
        if (request.getParentCommentId() != null) {
            Comment parent = findComment(request.getParentCommentId());
            if (!parent.getPostId().equals(request.getPostId())) {
                throw new IllegalArgumentException("Parent comment does not belong to this post");
            }
            if (parent.getParentCommentId() != null) {
                throw new IllegalArgumentException("Only two-level threading is supported");
            }
            if (parent.getStatus() == CommentStatus.DELETED) {
                throw new IllegalArgumentException("Cannot reply to deleted comment");
            }
        }

        Comment comment = Comment.builder()
                .postId(request.getPostId())
                .authorId(request.getAuthorId())
                .parentCommentId(request.getParentCommentId())
                .content(request.getContent().trim())
                .status(moderationMode ? CommentStatus.PENDING : CommentStatus.APPROVED)
                .build();
        comment = commentRepository.save(comment);
        return toResponse(comment);
    }

    @Override
    public List<CommentResponse> getCommentsByPost(Long postId) {
        return commentRepository.findByPostId(postId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CommentResponse getCommentById(Long commentId) {
        return toResponse(findComment(commentId));
    }

    @Override
    public List<CommentResponse> getReplies(Long commentId) {
        findComment(commentId);
        return commentRepository.findByParentCommentId(commentId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentUpdateRequest request) {
        Comment comment = findComment(commentId);
        if (!comment.getAuthorId().equals(request.getAuthorId())) {
            throw new IllegalArgumentException("You can only edit your own comment");
        }
        if (comment.getStatus() == CommentStatus.DELETED) {
            throw new IllegalArgumentException("Deleted comment cannot be edited");
        }
        long minutes = Duration.between(comment.getCreatedAt(), LocalDateTime.now()).toMinutes();
        if (minutes > 30) {
            throw new IllegalArgumentException("Comment edit window has expired");
        }
        comment.setContent(request.getContent().trim());
        comment = commentRepository.save(comment);
        return toResponse(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long actorId) {
        Comment comment = findComment(commentId);
        if (!comment.getAuthorId().equals(actorId)) {
            throw new IllegalArgumentException("You can only delete your own comment");
        }
        softDelete(comment);
        if (comment.getParentCommentId() == null) {
            List<Comment> replies = commentRepository.findByParentCommentId(comment.getCommentId());
            replies.forEach(this::softDelete);
            commentRepository.saveAll(replies);
        }
        commentRepository.save(comment);
    }

    @Override
    @Transactional
    public CommentResponse approveComment(Long commentId) {
        Comment comment = findComment(commentId);
        comment.setStatus(CommentStatus.APPROVED);
        comment = commentRepository.save(comment);
        return toResponse(comment);
    }

    @Override
    @Transactional
    public CommentResponse rejectComment(Long commentId) {
        Comment comment = findComment(commentId);
        comment.setStatus(CommentStatus.REJECTED);
        comment = commentRepository.save(comment);
        return toResponse(comment);
    }

    @Override
    @Transactional
    public void likeComment(Long commentId, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        Comment comment = findComment(commentId);
        if (comment.getStatus() == CommentStatus.DELETED) {
            throw new IllegalArgumentException("Comment is deleted");
        }
        if (commentLikeRepository.findByCommentIdAndUserId(commentId, userId).isPresent()) {
            return;
        }
        commentLikeRepository.save(CommentLike.builder().commentId(commentId).userId(userId).build());
        commentRepository.incrementLikes(commentId);
    }

    @Override
    @Transactional
    public void unlikeComment(Long commentId, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        findComment(commentId);
        CommentLike existing = commentLikeRepository.findByCommentIdAndUserId(commentId, userId).orElse(null);
        if (existing == null) {
            return;
        }
        commentLikeRepository.delete(existing);
        commentRepository.decrementLikes(commentId);
    }

    @Override
    public long getCommentCount(Long postId) {
        return commentRepository.countByPostId(postId);
    }

    @Override
    public boolean getModerationMode() {
        initializeModerationModeIfNeeded();
        return moderationMode;
    }

    @Override
    public void updateModerationMode(ModerationModeRequest request) {
        initializeModerationModeIfNeeded();
        moderationMode = request.isModerationRequired();
    }

    private void softDelete(Comment comment) {
        comment.setStatus(CommentStatus.DELETED);
        comment.setContent("[deleted]");
    }

    private Comment findComment(Long commentId) {
        return commentRepository.findByCommentId(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
    }

    private void initializeModerationModeIfNeeded() {
        if (!moderationModeInitialized) {
            moderationMode = moderationRequired;
            moderationModeInitialized = true;
        }
    }

    private CommentResponse toResponse(Comment comment) {
        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .postId(comment.getPostId())
                .authorId(comment.getAuthorId())
                .parentCommentId(comment.getParentCommentId())
                .content(comment.getContent())
                .likesCount(comment.getLikesCount())
                .status(comment.getStatus())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
