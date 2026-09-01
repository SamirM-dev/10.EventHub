package com.example.eventhub.auth.dto;

import com.example.eventhub.enums.UserRole;
import com.example.eventhub.validation.ValidUserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(@NotBlank(message = "User's name must be entered")@Size(min = 3,max = 50,message = "User's name length must be between 3 and 50 symbols")
                              String name,
                              @NotBlank(message = "Email must be entered")@Email(message = "Invalid email format")
                              String email,
                              @NotBlank(message = "Password must be entered")@Size(min = 8,message = "Password should has min 8 symbols")
                              String password,
                              @NotNull(message = "Role must be entered")@ValidUserRole(message = "Invalid user's role")
                              UserRole role) {
}
