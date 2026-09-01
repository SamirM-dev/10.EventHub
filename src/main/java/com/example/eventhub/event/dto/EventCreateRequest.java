package com.example.eventhub.event.dto;

import com.example.eventhub.enums.EventCategory;
import com.example.eventhub.helper.HasEventTimeRange;
import com.example.eventhub.validation.ValidEventCategory;
import com.example.eventhub.validation.ValidEventTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@ValidEventTime
public record EventCreateRequest(
        @NotBlank(message = "The title must be entered") String title,
        @NotBlank(message = "The description must be entered") String description,
        @NotNull(message = "The category must be entered")@ValidEventCategory(message = "Invalid event's category") EventCategory category,
        @NotBlank(message = "The venue must be entered") String venue,
        @NotNull(message = "The starting time must be entered")@JsonProperty("start_time") LocalDateTime startTime,
        @NotNull(message = "The ending time must be entered")@JsonProperty("end_time") LocalDateTime endTime,
        @NotNull(message = "The capacity must be entered")@Min(value = 1,message = "The capacity must 1 at least") int capacity,
        @NotNull(message = "The price must be entered")@DecimalMin(value = "0.0",message = "The price must 0.0 at least")BigDecimal price
        ) implements HasEventTimeRange {
}
