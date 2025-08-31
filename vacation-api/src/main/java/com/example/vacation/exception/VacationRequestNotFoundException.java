package com.example.vacation.exception;

public class VacationRequestNotFoundException extends RuntimeException {

    public VacationRequestNotFoundException(Long id) {
        super("Vacation request not found with ID: " + id);
    }
}
