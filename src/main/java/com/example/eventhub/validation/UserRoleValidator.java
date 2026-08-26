package com.example.eventhub.validation;

import com.example.eventhub.enums.UserRole;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class UserRoleValidator implements ConstraintValidator<ValidUserRole, UserRole> {

    private static final List<UserRole> ALLOWER_ROLES=List.of(UserRole.USER,UserRole.ORGANIZER);

    @Override
    public boolean isValid(UserRole value, ConstraintValidatorContext context) {
        if (value==null){
            return true;
        }
        return ALLOWER_ROLES.contains(value);
    }
}
