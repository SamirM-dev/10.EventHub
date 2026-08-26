package com.example.eventhub.review.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        @JsonProperty("event_id") Long eventId,
        @JsonProperty("user_id") Long userId,
        int rating,
        String comment,
        @JsonProperty("created_at") LocalDateTime createdAt) {
}
