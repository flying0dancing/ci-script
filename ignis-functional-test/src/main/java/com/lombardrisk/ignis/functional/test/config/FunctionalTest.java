package com.lombardrisk.ignis.functional.test.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@SpringBootTest(classes = { StepConfig.class, DesignStudioStepConfig.class })
@Import({ FunctionalTestClientConfig.class, DesignStudioClientConfig.class })
@Target(TYPE)
@Retention(RUNTIME)
public @interface FunctionalTest {
    // no-op
}
