package com.lombardrisk.ignis.test.config;

import com.lombardrisk.ignis.hadoop.HdfsTemplate;
import com.lombardrisk.ignis.server.batch.JobOperatorImpl;
import com.lombardrisk.ignis.server.config.job.HadoopConfiguration;
import com.lombardrisk.ignis.server.config.job.SparkDefaultsConfiguration;
import com.lombardrisk.ignis.server.health.MemoizedHealthCheck;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.Paths;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@Import(MockMvcConfiguration.class)
public class MockBeanConfiguration {

    static {
        if (SystemUtils.IS_OS_WINDOWS) {
            System.setProperty(
                    "hadoop.home.dir",
                    Paths.get("../ignis-core/ignis-common/src/test/resources/hadoop")
                            .toAbsolutePath()
                            .toString());
        }
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Bean
    public UserTestFixture userTestFixture() {
        return new UserTestFixture(passwordEncoder, jdbcTemplate);
    }

    @Bean
    @Primary
    public HadoopConfiguration hadoopConfiguration() {
        return mock(HadoopConfiguration.class);
    }

    @Bean
    @Primary
    public HdfsTemplate fileSystemTemplate() {
        return mock(HdfsTemplate.class);
    }

    @Bean
    @Primary
    public MemoizedHealthCheck yarnHealthCheck() {
        MemoizedHealthCheck memoizedHealthCheck = mock(MemoizedHealthCheck.class);
        when(memoizedHealthCheck.health())
                .thenReturn(Health.up().build());
        return memoizedHealthCheck;
    }

    @Bean
    @Primary
    public MemoizedHealthCheck hdfsHealthCheck() {
        MemoizedHealthCheck memoizedHealthCheck = mock(MemoizedHealthCheck.class);
        when(memoizedHealthCheck.health())
                .thenReturn(Health.up().build());
        return memoizedHealthCheck;
    }

    @Bean
    @Primary
    public SparkDefaultsConfiguration sparkBean() {
        return mock(SparkDefaultsConfiguration.class);
    }

    @Bean
    @Primary
    public JobOperatorImpl ignisJobOperator() {
        return mock(JobOperatorImpl.class);
    }

    @Bean
    public JobExplorer jobExplorer() {
        return mock(JobExplorer.class);
    }

    @Bean
    public org.springframework.batch.core.launch.JobOperator springBatchJobOperator() {
        return mock(org.springframework.batch.core.launch.JobOperator.class);
    }
}
