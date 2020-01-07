package com.lombardrisk.ignis.server.health;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import javax.sql.DataSource;
import java.time.Duration;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseHealthChecksTest {

    @Mock
    private DataSource dataSource;

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void health_CannotCreateConnection_ReturnsDownWithException() throws Exception {
        when(dataSource.getConnection())
                .thenThrow(new CannotGetJdbcConnectionException("I am a tester"));

        MemoizedHealthCheck dbHealthCheck = DatabaseHealthChecks.dbHealthCheck(dataSource, 2, Duration.ofSeconds(1));

        Health health = dbHealthCheck.health();

        soft.assertThat(health.getStatus())
                .isEqualTo(Status.DOWN);
        soft.assertThat(health.getDetails())
                .isEqualTo(ImmutableMap.of(
                        "error", "org.springframework.jdbc.CannotGetJdbcConnectionException: I am a tester"));
    }

    @Test
    public void health_CannotCreateConnection_MemoizedResult() throws Exception {
        when(dataSource.getConnection())
                .thenThrow(new CannotGetJdbcConnectionException("I am a tester"));

        MemoizedHealthCheck dbHealthCheck = DatabaseHealthChecks.dbHealthCheck(dataSource, 2, Duration.ofSeconds(1));

        Health health = dbHealthCheck.health();
        dbHealthCheck.health();
        dbHealthCheck.health();

        verify(dataSource, times(1)).getConnection();
    }
}