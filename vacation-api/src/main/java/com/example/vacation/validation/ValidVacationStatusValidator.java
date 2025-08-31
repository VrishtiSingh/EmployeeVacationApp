package com.example.vacation.validation;

import com.example.vacation.entity.VacationRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidVacationStatusValidator implements ConstraintValidator<ValidVacationStatus, VacationRequest.Status> {

    @Override
    public boolean isValid(VacationRequest.Status value, ConstraintValidatorContext context) {
        return value != null; // enum ensures only PENDING, APPROVED, REJECTED
    }
}
