package com.example.eventhub.event.dto;

import com.example.eventhub.enums.EventCategory;
import com.example.eventhub.enums.EventStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record EventResponse(Long id,
                            String title,
                            String description,
                            EventCategory category,
                            String venue,
                            @JsonProperty("start_time") LocalDateTime startTime,
                            @JsonProperty("end_time")LocalDateTime endTime,
                            int capacity,
                            @JsonProperty("available_seats")int availableSeats,
                            BigDecimal price,
                            EventStatus status,
                            @JsonProperty("organizer_id")Long organizerId,
                            @JsonProperty("created_at")LocalDateTime createdAt) {
}
