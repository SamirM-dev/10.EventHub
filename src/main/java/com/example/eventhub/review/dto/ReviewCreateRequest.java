package com.example.eventhub.review.dto;

import jakarta.validation.constraints.*;

public record ReviewCreateRequest(
        @NotNull(message = "The rating must be entered")@Min(value = 1,message = "The rating must be in the range from 1 to 5")
        @Max(value = 5,message = "The rating must be in the range from 1 to 5") int rating,
        @NotBlank(message = "The comment must be entered")@Size(min = 1,max = 500,message ="The rating must be in the range from 1 to 500")
        String  comment) {
}
