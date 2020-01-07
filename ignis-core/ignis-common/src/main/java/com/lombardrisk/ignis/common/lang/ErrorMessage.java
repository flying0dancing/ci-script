package com.lombardrisk.ignis.common.lang;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorMessage implements Serializable {

    private static final long serialVersionUID = -8105834638560931135L;

    private final String message;

    public static ErrorMessage of(final String message) {
        return new ErrorMessage(message);
    }

    public static ErrorMessage of(final Throwable throwable) {
        return new ErrorMessage(errorMessageFormat(throwable));
    }

    private static String errorMessageFormat(final Throwable throwable) {
        return String.format("%s: %s", throwable.getClass().getName(), throwable.getMessage());
    }
}
