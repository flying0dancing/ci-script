package com.lombardrisk.ignis.server.dataset.config;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@AutoConfigureMockMvc
@SpringBootTest(classes = DatasetTestConfiguration.class)
@Transactional
@Rollback
@Target(TYPE)
@Retention(RUNTIME)
public @interface DatasetTest {
    //no-op
}
