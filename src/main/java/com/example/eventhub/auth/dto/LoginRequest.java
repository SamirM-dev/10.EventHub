package com.example.eventhub.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank(message = "Email must be entered")@Email(message = "Invalid email format")
                           String email,
                           @NotBlank(message = "Password must be entered")
                           String password) {
}
