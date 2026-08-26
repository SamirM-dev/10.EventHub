package com.example.eventhub.user.dto;

import com.example.eventhub.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        UserRole role,
        @JsonProperty("created_at")
        LocalDateTime createdAt) {
}
