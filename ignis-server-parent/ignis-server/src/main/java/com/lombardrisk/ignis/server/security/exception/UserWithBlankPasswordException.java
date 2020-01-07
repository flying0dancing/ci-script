package com.lombardrisk.ignis.server.security.exception;

import org.springframework.security.core.AuthenticationException;

public class UserWithBlankPasswordException extends AuthenticationException {

    private static final long serialVersionUID = -1683530671216825678L;

    public UserWithBlankPasswordException(String msg) {
        super(msg);
    }
}
