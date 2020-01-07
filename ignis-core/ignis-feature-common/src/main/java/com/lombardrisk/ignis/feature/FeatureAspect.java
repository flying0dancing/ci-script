package com.lombardrisk.ignis.feature;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.togglz.core.manager.FeatureManager;

@Aspect
@Slf4j
public class FeatureAspect {

    private final FeatureManager featureManager;

    public FeatureAspect(final FeatureManager featureManager) {
        this.featureManager = featureManager;
    }

    @Around(
            "@within(featureEnabled) || @annotation(featureEnabled)"
    )
    public Object checkAspect(
            final ProceedingJoinPoint joinPoint,
            final FeatureEnabled featureEnabled) throws Throwable {

        IgnisFeature feature = featureEnabled.feature();

        if (featureManager.isActive(feature)) {
            return joinPoint.proceed();
        } else {
            log.warn("Feature {} is not enabled!", feature.name());
            throw new FeatureNotActiveException(feature);
        }
    }
}
