package com.lombardrisk.ignis.server.security.exception;

public class UserNotFoundException extends UserSecurityException {

    public UserNotFoundException() {
    }

    public UserNotFoundException(String msg) {
        super(msg);
    }
}
