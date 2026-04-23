package com.app.newsletterservice.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.newsletterservice.dto.DispatchResponse;
import com.app.newsletterservice.dto.SendNewsletterRequest;
import com.app.newsletterservice.dto.SendPostNotificationRequest;
import com.app.newsletterservice.dto.SubscriberResponse;
import com.app.newsletterservice.dto.SubscribeRequest;
import com.app.newsletterservice.dto.UpdatePreferencesRequest;
import com.app.newsletterservice.entity.Subscriber;
import com.app.newsletterservice.entity.SubscriberStatus;
import com.app.newsletterservice.repository.SubscriberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsletterServiceImpl implements NewsletterService {

    private final SubscriberRepository subscriberRepository;

    @Override
    @Transactional
    public SubscriberResponse subscribe(SubscribeRequest request) {
        String email = normalizeEmail(request.getEmail());
        Subscriber subscriber = subscriberRepository.findByEmail(email).orElse(null);
        if (subscriber == null) {
            subscriber = Subscriber.builder()
                    .email(email)
                    .userId(request.getUserId())
                    .fullName(request.getFullName())
                    .preferences(joinPreferences(request.getPreferences()))
                    .status(SubscriberStatus.PENDING)
                    .token(newToken())
                    .tokenExpiresAt(LocalDateTime.now().plusHours(24))
                    .subscribedAt(LocalDateTime.now())
                    .build();
        } else {
            subscriber.setUserId(request.getUserId());
            subscriber.setFullName(request.getFullName());
            subscriber.setPreferences(joinPreferences(request.getPreferences()));
            subscriber.setStatus(SubscriberStatus.PENDING);
            subscriber.setToken(newToken());
            subscriber.setTokenExpiresAt(LocalDateTime.now().plusHours(24));
            subscriber.setSubscribedAt(LocalDateTime.now());
            subscriber.setUnsubscribedAt(null);
        }
        subscriber = subscriberRepository.save(subscriber);
        return toResponse(subscriber);
    }

    @Override
    @Transactional
    public DispatchResponse unsubscribe(String token) {
        Subscriber subscriber = findByToken(token);
        subscriber.setStatus(SubscriberStatus.UNSUBSCRIBED);
        subscriber.setUnsubscribedAt(LocalDateTime.now());
        subscriberRepository.save(subscriber);
        return new DispatchResponse("Unsubscribed successfully", 1);
    }

    @Override
    @Transactional
    public SubscriberResponse confirmSubscription(String token) {
        Subscriber subscriber = findByToken(token);
        if (subscriber.getTokenExpiresAt() == null || subscriber.getTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Confirmation token expired");
        }
        subscriber.setStatus(SubscriberStatus.ACTIVE);
        subscriber.setTokenExpiresAt(LocalDateTime.now().plusYears(2));
        subscriber = subscriberRepository.save(subscriber);
        return toResponse(subscriber);
    }

    @Override
    public SubscriberResponse getSubscriberByEmail(String email) {
        Subscriber subscriber = subscriberRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new IllegalArgumentException("Subscriber not found"));
        return toResponse(subscriber);
    }

    @Override
    public List<SubscriberResponse> getAllSubscribers() {
        return subscriberRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public DispatchResponse sendNewsletter(SendNewsletterRequest request) {
        SubscriberStatus statusFilter = request.getStatusFilter() == null ? SubscriberStatus.ACTIVE : request.getStatusFilter();
        List<Subscriber> recipients = subscriberRepository.findByStatus(statusFilter);
        if (request.getPreferenceFilter() != null && !request.getPreferenceFilter().isEmpty()) {
            Set<String> filters = request.getPreferenceFilter().stream()
                    .map(value -> value.toLowerCase(Locale.ROOT).trim())
                    .collect(Collectors.toSet());
            recipients = recipients.stream()
                    .filter(subscriber -> !disjoint(parsePreferences(subscriber.getPreferences()), filters))
                    .toList();
        }
        return new DispatchResponse("Newsletter queued", recipients.size());
    }

    @Override
    public DispatchResponse sendPostNotification(SendPostNotificationRequest request) {
        long count = subscriberRepository.countByStatus(SubscriberStatus.ACTIVE);
        return new DispatchResponse("Post notification queued", count);
    }

    @Override
    @Transactional
    public SubscriberResponse updatePreferences(UpdatePreferencesRequest request) {
        Subscriber subscriber = subscriberRepository.findByEmail(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new IllegalArgumentException("Subscriber not found"));
        subscriber.setPreferences(joinPreferences(request.getPreferences()));
        subscriber = subscriberRepository.save(subscriber);
        return toResponse(subscriber);
    }

    @Override
    public long getSubscriberCount(SubscriberStatus status) {
        if (status == null) {
            return subscriberRepository.count();
        }
        return subscriberRepository.countByStatus(status);
    }

    @Override
    public DispatchResponse sendWelcomeEmail(String email) {
        getSubscriberByEmail(email);
        return new DispatchResponse("Welcome email queued", 1);
    }

    private Subscriber findByToken(String token) {
        return subscriberRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
    }

    private String joinPreferences(List<String> preferences) {
        if (preferences == null || preferences.isEmpty()) {
            return "";
        }
        return preferences.stream()
                .map(value -> value == null ? "" : value.trim().toLowerCase(Locale.ROOT))
                .filter(value -> !value.isBlank())
                .distinct()
                .collect(Collectors.joining(","));
    }

    private Set<String> parsePreferences(String preferences) {
        if (preferences == null || preferences.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(preferences.split(","))
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .filter(value -> !value.isBlank())
                .collect(Collectors.toSet());
    }

    private boolean disjoint(Set<String> left, Set<String> right) {
        for (String value : left) {
            if (right.contains(value)) {
                return false;
            }
        }
        return true;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String newToken() {
        return UUID.randomUUID().toString();
    }

    private SubscriberResponse toResponse(Subscriber subscriber) {
        return SubscriberResponse.builder()
                .subscriberId(subscriber.getSubscriberId())
                .email(subscriber.getEmail())
                .userId(subscriber.getUserId())
                .fullName(subscriber.getFullName())
                .status(subscriber.getStatus())
                .subscribedAt(subscriber.getSubscribedAt())
                .unsubscribedAt(subscriber.getUnsubscribedAt())
                .token(subscriber.getToken())
                .tokenExpiresAt(subscriber.getTokenExpiresAt())
                .preferences(parsePreferences(subscriber.getPreferences()).stream().toList())
                .build();
    }
}
