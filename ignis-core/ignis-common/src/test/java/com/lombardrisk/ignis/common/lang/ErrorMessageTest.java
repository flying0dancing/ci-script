package com.lombardrisk.ignis.common.lang;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorMessageTest {

    @Test
    public void ofThrowable_ReturnsErrorMessageWithClassAndMessage() {
        ErrorMessage errorMessage = ErrorMessage.of(new RuntimeException("OOPS"));
        assertThat(errorMessage.getMessage())
                .isEqualTo("java.lang.RuntimeException: OOPS");
    }
}