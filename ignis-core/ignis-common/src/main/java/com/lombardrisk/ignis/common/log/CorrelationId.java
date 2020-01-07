package com.lombardrisk.ignis.common.log;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;

@UtilityClass
public class CorrelationId {
    private static final String CORRELATION_ID_LOG_VAR_NAME = "correlationId";
    public static final String CORRELATION_ID_HEADER = "Correlation-Id";

    private static ThreadLocal<String> localCorrelationId = new ThreadLocal<>();

    public static String getCorrelationId() {
        return localCorrelationId.get();
    }

    public static void setCorrelationId(final String correlationId) {
        localCorrelationId.set(correlationId);
        MDC.put(CORRELATION_ID_LOG_VAR_NAME, correlationId);
    }

    public static void cleanup() {
        localCorrelationId.remove();
        MDC.remove(CORRELATION_ID_LOG_VAR_NAME);
    }
}
