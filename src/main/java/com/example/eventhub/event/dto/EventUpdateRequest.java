package com.example.eventhub.event.dto;

import com.example.eventhub.enums.EventCategory;
import com.example.eventhub.enums.EventStatus;
import com.example.eventhub.validation.ValidEventCategory;
import com.example.eventhub.validation.ValidEventStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventUpdateRequest(@NotBlank(message = "The title must be entered") String title,
                                 @NotBlank(message = "The description must be entered") String description,
                                 @NotBlank(message = "The category must be entered")@ValidEventCategory(message = "Invalid event's category") EventCategory category,
                                 @NotBlank(message = "The venue must be entered") String venue,
                                 @NotNull(message = "The starting time must be entered")@JsonProperty("start_time") LocalDateTime startTime,
                                 @NotNull(message = "The ending time must be entered")@JsonProperty("end_time") LocalDateTime endTime,
                                 @NotNull(message = "The capacity must be entered")@Min(value = 1,message = "The capacity must 1 at least") int capacity,
                                 @NotNull(message = "The price must be entered")@Min(value = 0,message = "The price must 0.0 at least") BigDecimal price,
                                 @NotBlank(message = "The status must be entered")@ValidEventStatus(message = "Invalid event's status") EventStatus status) {
}
