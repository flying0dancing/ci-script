package com.lombardrisk.ignis.server.health;

import com.google.common.base.Suppliers;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemoizedHealthCheck implements HealthIndicator {

    private static final String DEFAULT_QUERY = "SELECT 1";
    private final Supplier<Health> checker;

    public static MemoizedHealthCheck create(final Supplier<Health> healthSupplier, final Duration healthRefreshRate) {

        Supplier<Health> checker = Suppliers.memoizeWithExpiration(
                healthSupplier::get, healthRefreshRate.get(ChronoUnit.SECONDS), TimeUnit.SECONDS);

        return new MemoizedHealthCheck(checker);
    }

    @Override
    public Health health() {
        return checker.get();
    }
}
