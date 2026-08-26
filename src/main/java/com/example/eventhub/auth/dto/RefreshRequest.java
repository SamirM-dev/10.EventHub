package com.example.eventhub.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

public record RefreshRequest(
        @JsonFormat(pattern = "refresh_token") String refreshToken
) {
}
