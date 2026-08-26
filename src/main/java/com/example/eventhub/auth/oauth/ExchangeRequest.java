package com.example.eventhub.auth.oauth;

import jakarta.validation.constraints.NotBlank;

public record ExchangeRequest(@NotBlank String code) {
}
