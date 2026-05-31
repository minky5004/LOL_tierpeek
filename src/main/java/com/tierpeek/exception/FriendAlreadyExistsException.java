package com.tierpeek.exception;

public class FriendAlreadyExistsException extends RuntimeException {
    public FriendAlreadyExistsException(String message) {
        super(message);
    }

    public FriendAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
