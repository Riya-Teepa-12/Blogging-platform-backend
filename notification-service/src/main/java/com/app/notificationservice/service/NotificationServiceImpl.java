package com.app.notificationservice.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.notificationservice.dto.BulkNotificationRequest;
import com.app.notificationservice.dto.DispatchResponse;
import com.app.notificationservice.dto.NotificationRequest;
import com.app.notificationservice.dto.NotificationResponse;
import com.app.notificationservice.entity.Notification;
import com.app.notificationservice.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public NotificationResponse send(NotificationRequest request) {
        Notification notification = notificationRepository.save(Notification.builder()
                .recipientId(request.getRecipientId())
                .actorId(request.getActorId())
                .type(request.getType())
                .title(request.getTitle())
                .message(request.getMessage())
                .relatedId(request.getRelatedId())
                .relatedType(request.getRelatedType())
                .isRead(false)
                .build());
        return toResponse(notification);
    }

    @Override
    @Transactional
    public DispatchResponse sendBulk(BulkNotificationRequest request) {
        List<Notification> notifications = request.getRecipientIds().stream()
                .map(recipientId -> Notification.builder()
                        .recipientId(recipientId)
                        .actorId(request.getActorId())
                        .type(request.getType())
                        .title(request.getTitle())
                        .message(request.getMessage())
                        .relatedId(request.getRelatedId())
                        .relatedType(request.getRelatedType())
                        .isRead(false)
                        .build())
                .toList();
        notificationRepository.saveAll(notifications);
        return new DispatchResponse("Notifications sent", notifications.size());
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId) {
        Notification notification = find(notificationId);
        notification.setIsRead(true);
        notification = notificationRepository.save(notification);
        return toResponse(notification);
    }

    @Override
    @Transactional
    public DispatchResponse markAllRead(Long recipientId) {
        List<Notification> unread = notificationRepository.findByRecipientIdAndIsRead(recipientId, false);
        unread.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unread);
        return new DispatchResponse("Marked all as read", unread.size());
    }

    @Override
    @Transactional
    public DispatchResponse deleteRead(Long recipientId) {
        long count = notificationRepository.findByRecipientIdAndIsRead(recipientId, true).size();
        notificationRepository.deleteByRecipientIdAndIsRead(recipientId, true);
        return new DispatchResponse("Deleted read notifications", count);
    }

    @Override
    public List<NotificationResponse> getByRecipient(Long recipientId) {
        return notificationRepository.findByRecipientId(recipientId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public long getUnreadCount(Long recipientId) {
        return notificationRepository.countByRecipientIdAndIsRead(recipientId, false);
    }

    @Override
    @Transactional
    public DispatchResponse deleteNotification(Long notificationId) {
        find(notificationId);
        notificationRepository.deleteByNotificationId(notificationId);
        return new DispatchResponse("Notification deleted", 1);
    }

    @Override
    public DispatchResponse sendEmail(NotificationRequest request) {
        return new DispatchResponse("Email notification queued", 1);
    }

    @Override
    public List<NotificationResponse> getAll() {
        return notificationRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private Notification find(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .recipientId(notification.getRecipientId())
                .actorId(notification.getActorId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .relatedId(notification.getRelatedId())
                .relatedType(notification.getRelatedType())
                .read(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
