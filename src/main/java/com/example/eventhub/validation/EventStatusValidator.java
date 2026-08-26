package com.example.eventhub.validation;

import com.example.eventhub.enums.EventStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class EventStatusValidator implements ConstraintValidator<ValidEventStatus, EventStatus> {

    private static final List<EventStatus> ALLOWER_STATUSES=List.of(EventStatus.DRAFT,EventStatus.PUBLISHED,EventStatus.COMPLETED,EventStatus.CANCELLED);

    @Override
    public boolean isValid(EventStatus value, ConstraintValidatorContext context) {
        if (value==null){
            return true;
        }
        return ALLOWER_STATUSES.contains(value);
    }
}
