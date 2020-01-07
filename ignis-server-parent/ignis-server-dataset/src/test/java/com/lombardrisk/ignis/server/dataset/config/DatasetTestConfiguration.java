package com.lombardrisk.ignis.server.dataset.config;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = { "com.lombardrisk.ignis.server.dataset", "com.lombardrisk.ignis.server.product" })
@EnableJpaRepositories(basePackages = { "com.lombardrisk.ignis.server.dataset", "com.lombardrisk.ignis.server.product" })
public class DatasetTestConfiguration {

    public static void main(final String[] args) {
        new SpringApplicationBuilder(DatasetTestConfiguration.class, DataSourceConfiguration.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }
}
