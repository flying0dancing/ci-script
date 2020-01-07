package com.lombardrisk.ignis.server.eraser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.lombardrisk.ignis.server.eraser.config")
@EnableJpaRepositories(basePackages = {
        "com.lombardrisk.ignis.server.product",
        "com.lombardrisk.ignis.server.dataset"
})
@EntityScan(basePackages = {
        "com.lombardrisk.ignis.server.product",
        "com.lombardrisk.ignis.server.dataset"
})
@Slf4j
public class IgnisEraserApplication {

    public static void main(final String[] args) {
        SpringApplication.run(IgnisEraserApplication.class, args);
    }
}
