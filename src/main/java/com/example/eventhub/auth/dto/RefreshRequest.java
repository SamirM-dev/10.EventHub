package com.example.eventhub.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @JsonFormat(pattern = "refresh_token")@NotBlank String refreshToken
) {
}
