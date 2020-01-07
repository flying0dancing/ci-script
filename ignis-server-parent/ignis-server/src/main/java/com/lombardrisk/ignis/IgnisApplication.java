package com.lombardrisk.ignis;

import com.lombardrisk.ignis.common.annotation.ExcludeFromTest;
import com.lombardrisk.ignis.server.config.ApplicationConf;
import com.lombardrisk.ignis.web.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackageClasses = { ApplicationConf.class, GlobalExceptionHandler.class })
@EnableJpaRepositories
@ExcludeFromTest
public class IgnisApplication {

    public static void main(final String[] args) {
        SpringApplication.run(IgnisApplication.class, args);
    }
}
