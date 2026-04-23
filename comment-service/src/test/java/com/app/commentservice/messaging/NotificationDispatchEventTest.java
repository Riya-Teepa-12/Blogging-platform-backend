package com.app.commentservice.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

class NotificationDispatchEventTest {

    @Test
    void builderAndAccessorsWork() {
        Instant now = Instant.now();
        NotificationDispatchEvent event = NotificationDispatchEvent.builder()
                .eventId("evt-1")
                .sourceService("comment-service")
                .dispatchChannel("IN_APP")
                .recipientId(10L)
                .recipientIds(List.of(10L, 11L))
                .actorId(1L)
                .type("MENTION")
                .title("title")
                .message("message")
                .relatedId(5L)
                .relatedType("COMMENT")
                .occurredAt(now)
                .build();

        assertThat(event.getEventId()).isEqualTo("evt-1");
        assertThat(event.getRecipientIds()).containsExactly(10L, 11L);
        assertThat(event.getOccurredAt()).isEqualTo(now);

        NotificationDispatchEvent mutable = new NotificationDispatchEvent();
        mutable.setDispatchChannel("EMAIL");
        mutable.setRecipientId(99L);
        assertThat(mutable.getDispatchChannel()).isEqualTo("EMAIL");
        assertThat(mutable.getRecipientId()).isEqualTo(99L);
    }
}
