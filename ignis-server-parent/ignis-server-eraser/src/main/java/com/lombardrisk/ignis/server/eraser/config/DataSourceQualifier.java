package com.lombardrisk.ignis.server.eraser.config;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Qualifier
@java.lang.annotation.Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface DataSourceQualifier {

    Target value();

    enum Target {
        PHOENIX,
        MASTER
    }
}
