package com.example.eventhub.booking.dto;

import com.example.eventhub.enums.BookingStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        @JsonProperty("event_id") Long eventId,
        @JsonProperty("user_id")Long userId,
        int quantity,
        @JsonProperty("total_price")BigDecimal totalPrice,
        BookingStatus status,
        @JsonProperty("booked_at")LocalDateTime bookedAt) {
}
