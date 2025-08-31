package com.example.vacation.exception;

public class NotEnoughVacationDaysException extends RuntimeException {

    public NotEnoughVacationDaysException(String message) {
        super(message);
    }
}
