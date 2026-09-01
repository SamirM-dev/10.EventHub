package com.example.eventhub.validation;

import com.example.eventhub.event.dto.EventCreateRequest;
import com.example.eventhub.helper.HasEventTimeRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EventTimeValidator implements ConstraintValidator<ValidEventTime, HasEventTimeRange> {

    @Override
    public boolean isValid(HasEventTimeRange value, ConstraintValidatorContext context) {
        if(value.startTime()==null||value.endTime()==null){
            return true;
        }
        return value.endTime().isAfter(value.startTime());
    }
}
