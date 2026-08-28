package com.example.eventhub.validation;

import com.example.eventhub.event.dto.EventCreateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EventTimeValidator implements ConstraintValidator<ValidEventTime,EventCreateRequest> {

    @Override
    public boolean isValid(EventCreateRequest value, ConstraintValidatorContext context) {
        if(value.startTime()==null||value.endTime()==null){
            return true;
        }
        return value.endTime().isAfter(value.startTime());
    }
}
