package com.lombardrisk.ignis.design.server;

import com.lombardrisk.ignis.design.server.configuration.DesignConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = "com.lombardrisk.ignis.design")
@EnableJpaRepositories
@SpringBootApplication(
        scanBasePackageClasses = DesignConfiguration.class,
        exclude = { SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class })
public class IgnisDesignApplication {

    public static void main(final String[] args) {
        SpringApplication.run(IgnisDesignApplication.class, args);
    }
}
