package com.lombardrisk.ignis;

import com.lombardrisk.ignis.util.FieldUtil;
import com.lombardrisk.ignis.util.PipelineStepSelectUtil;
import com.lombardrisk.ignis.util.PipelineStepUtil;
import com.lombardrisk.ignis.util.PipelineUtil;
import com.lombardrisk.ignis.util.ProductConfigUtil;
import com.lombardrisk.ignis.util.TableUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@SpringBootApplication
public class TestFlywayApplication {

    @Autowired
    private DataSource dataSource;

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public ProductConfigUtil productConfigUtil() {
        return new ProductConfigUtil(jdbcTemplate());
    }

    @Bean
    public TableUtil tableUtil() {
        return new TableUtil(jdbcTemplate(), fieldUtil());
    }

    @Bean
    public FieldUtil fieldUtil() {
        return new FieldUtil(jdbcTemplate());
    }

    @Bean
    public PipelineUtil pipelineUtil() {
        return new PipelineUtil(jdbcTemplate(), pipelineStepUtil());
    }

    @Bean
    public PipelineStepUtil pipelineStepUtil() {
        return new PipelineStepUtil(jdbcTemplate(), pipelineStepSelectUtil());
    }

    @Bean
    public PipelineStepSelectUtil pipelineStepSelectUtil() {
        return new PipelineStepSelectUtil(jdbcTemplate());
    }
}
