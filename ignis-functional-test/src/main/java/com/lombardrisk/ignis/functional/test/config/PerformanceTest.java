package com.lombardrisk.ignis.functional.test.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@SpringBootTest(classes = StepConfig.class)
@Import(PerformanceTestConfig.class)
@Target(TYPE)
@Retention(RUNTIME)
public @interface PerformanceTest {

}
