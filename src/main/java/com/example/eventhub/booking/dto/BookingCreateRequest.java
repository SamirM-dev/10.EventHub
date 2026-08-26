package com.example.eventhub.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BookingCreateRequest(
        @NotNull(message = "The quantity must be entered")@Min(value = 1,message = "The quantity must be 1 at least") int quantity
) {
}
