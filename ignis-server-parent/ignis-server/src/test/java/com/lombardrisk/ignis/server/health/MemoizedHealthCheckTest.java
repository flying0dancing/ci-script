package com.lombardrisk.ignis.server.health;

import org.junit.Test;
import org.springframework.boot.actuate.health.Health;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

public class MemoizedHealthCheckTest {

    @Test
    public void health_ResultExpired_QueriesHealthAgain() throws Exception {
        AtomicLong count = new AtomicLong();
        MemoizedHealthCheck healthCheck = MemoizedHealthCheck.create(
                () -> {
                    count.incrementAndGet();
                    return Health.up().build();
                },
                Duration.ofSeconds(1));

        healthCheck.health();
        Thread.sleep(1010);

        healthCheck.health();

        assertThat(count.get())
                .isEqualTo(2);
    }
}