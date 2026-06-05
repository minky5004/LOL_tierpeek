package com.tierpeek.exception;

public class SchedulerAlreadyRunningException extends RuntimeException {
    public SchedulerAlreadyRunningException(String message) {
        super(message);
    }
}