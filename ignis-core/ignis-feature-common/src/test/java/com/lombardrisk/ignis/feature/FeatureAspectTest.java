package com.lombardrisk.ignis.feature;

import org.aspectj.lang.ProceedingJoinPoint;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.togglz.core.manager.FeatureManager;

import java.lang.annotation.Annotation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FeatureAspectTest {

    @Mock
    private FeatureManager featureManager;

    @InjectMocks
    private FeatureAspect featureAspect;

    @Before
    public void setUp() {
        when(featureManager.isActive(any()))
                .thenReturn(true);
    }

    @Test
    public void checkAspect_featureEnabled_proceeds() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        featureAspect.checkAspect(joinPoint, new TestFeatureEnabledAnnotation(IgnisFeature.APPEND_DATASETS));

        verify(joinPoint).proceed();
    }

    @Test
    public void checkAspect_featureDisabled_ThrowsFeatureException() {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        when(featureManager.isActive(any()))
                .thenReturn(false);

        Assertions.assertThatThrownBy(() ->
                featureAspect.checkAspect(
                        joinPoint,
                        new TestFeatureEnabledAnnotation(IgnisFeature.APPEND_DATASETS))
        ).isInstanceOf(FeatureNotActiveException.class)
                .hasMessage("Feature APPEND_DATASETS is not active");
    }

    @Test
    public void checkAspect_featureDisabled_DoesNotProceedWithInvocation() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        when(featureManager.isActive(any()))
                .thenReturn(false);

        Assertions.assertThatThrownBy(() ->
                featureAspect.checkAspect(
                        joinPoint,
                        new TestFeatureEnabledAnnotation(IgnisFeature.APPEND_DATASETS))
        );

        verify(joinPoint, never()).proceed();
    }

    private static final class TestFeatureEnabledAnnotation implements FeatureEnabled {

        private final IgnisFeature ignisFeature;

        private TestFeatureEnabledAnnotation(final IgnisFeature ignisFeature) {
            this.ignisFeature = ignisFeature;
        }

        @Override
        public IgnisFeature feature() {
            return ignisFeature;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return null;
        }
    }
}
