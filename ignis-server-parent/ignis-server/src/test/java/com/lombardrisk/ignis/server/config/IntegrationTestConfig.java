package com.lombardrisk.ignis.server.config;

import com.lombardrisk.ignis.TestIgnisApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@AutoConfigureMockMvc
@SpringBootTest(classes = TestIgnisApplication.class)
@TestPropertySource(properties = {
        "IGNIS_HOST=localhost",
        "ignis.host=localhost",
        "env.hostname=localhost",
        "ignis.home=.",
        "phoenix.datasource.url=jdbc:h2:mem:test_phoenix;DB_CLOSE_ON_EXIT=FALSE",
        "phoenix.datasource.driver-class-name=org.h2.Driver"
})
@Transactional
@Rollback
@Target(TYPE)
@Retention(RUNTIME)
public @interface IntegrationTestConfig {
    //no-op
}
