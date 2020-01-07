package com.lombardrisk.ignis.server.security.exception;

public class UserSecurityException extends Exception {

    public UserSecurityException() {
    }

    public UserSecurityException(String msg) {
        super(msg);
    }

    public UserSecurityException(String msg, Throwable t) {
        super(msg, t);
    }
}
