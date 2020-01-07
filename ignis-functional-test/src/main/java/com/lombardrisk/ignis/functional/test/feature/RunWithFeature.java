package com.lombardrisk.ignis.functional.test.feature;

import com.lombardrisk.ignis.feature.IgnisFeature;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.METHOD,
        ElementType.TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface RunWithFeature {

    IgnisFeature feature();

    boolean active();
}
