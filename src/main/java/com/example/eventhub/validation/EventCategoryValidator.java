package com.example.eventhub.validation;

import com.example.eventhub.enums.EventCategory;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class EventCategoryValidator implements ConstraintValidator<ValidEventCategory, EventCategory> {

    private static final List<EventCategory> ALLOWER_CATEGORIES=List.of(EventCategory.CONFERENCE,EventCategory.CONCERT,EventCategory.SPORTS,EventCategory.WORKSHOP,EventCategory.OTHER);

    @Override
    public boolean isValid(EventCategory value, ConstraintValidatorContext context) {
        if (value==null){
            return true;
        }
        return ALLOWER_CATEGORIES.contains(value);
    }
}
