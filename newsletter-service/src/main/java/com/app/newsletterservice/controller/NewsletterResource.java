package com.app.newsletterservice.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.newsletterservice.dto.DispatchResponse;
import com.app.newsletterservice.dto.SendNewsletterRequest;
import com.app.newsletterservice.dto.SendPostNotificationRequest;
import com.app.newsletterservice.dto.SubscriberResponse;
import com.app.newsletterservice.dto.SubscribeRequest;
import com.app.newsletterservice.dto.UpdatePreferencesRequest;
import com.app.newsletterservice.entity.SubscriberStatus;
import com.app.newsletterservice.service.NewsletterService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/newsletter")
@RequiredArgsConstructor
public class NewsletterResource {

    private final NewsletterService newsletterService;

    @PostMapping("/subscribe")
    public SubscriberResponse subscribe(@Valid @RequestBody SubscribeRequest request) {
        return newsletterService.subscribe(request);
    }

    @GetMapping("/confirm")
    public SubscriberResponse confirm(@RequestParam String token) {
        return newsletterService.confirmSubscription(token);
    }

    @GetMapping("/unsubscribe")
    public DispatchResponse unsubscribe(@RequestParam String token) {
        return newsletterService.unsubscribe(token);
    }

    @GetMapping("/all")
    public List<SubscriberResponse> getAll() {
        return newsletterService.getAllSubscribers();
    }

    @PostMapping("/send-newsletter")
    public DispatchResponse sendNewsletter(@Valid @RequestBody SendNewsletterRequest request) {
        return newsletterService.sendNewsletter(request);
    }

    @PostMapping("/send-post-notification")
    public DispatchResponse sendPostNotification(@Valid @RequestBody SendPostNotificationRequest request) {
        return newsletterService.sendPostNotification(request);
    }

    @PutMapping("/preferences")
    public SubscriberResponse updatePreferences(@Valid @RequestBody UpdatePreferencesRequest request) {
        return newsletterService.updatePreferences(request);
    }

    @PostMapping("/send-welcome")
    public DispatchResponse sendWelcome(@RequestParam String email) {
        return newsletterService.sendWelcomeEmail(email);
    }

    @GetMapping("/count")
    public Map<String, Long> count(@RequestParam(required = false) SubscriberStatus status) {
        return Map.of("count", newsletterService.getSubscriberCount(status));
    }
}
