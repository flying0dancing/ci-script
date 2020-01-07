package com.lombardrisk.ignis.common.log;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CorrelationIdTest {

    @Before
    public void setUp() {
        CorrelationId.cleanup();
    }

    @Test
    public void setAndGet() {
        CorrelationId.setCorrelationId("hello");

        assertThat(CorrelationId.getCorrelationId())
                .isEqualTo("hello");
    }

    @Test
    public void cleanup() {
        CorrelationId.cleanup();

        CorrelationId.setCorrelationId("should not be set");
        CorrelationId.cleanup();

        assertThat(CorrelationId.getCorrelationId()).isNull();
    }
}