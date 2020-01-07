package com.lombardrisk.ignis.functional.test.feature;

import com.lombardrisk.ignis.feature.IgnisFeature;
import lombok.extern.slf4j.Slf4j;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.function.Supplier;

@Slf4j
public final class FeatureRule extends TestWatcher {

    private final Supplier<FeatureService> featureService;

    public FeatureRule(final Supplier<FeatureService> featureService) {
        this.featureService = featureService;
    }

    @Override
    public Statement apply(
            final Statement base, final Description description) {
        RunWithFeature runWithFeature = description.getAnnotation(RunWithFeature.class);
        if (runWithFeature != null) {
            IgnisFeature feature = runWithFeature.feature();
            updateFeature(feature, runWithFeature.active());
        }

        return super.apply(base, description);
    }

    @Override
    protected void finished(final Description description) {
        RunWithFeature runWithFeature = description.getAnnotation(RunWithFeature.class);
        if (runWithFeature != null) {
            IgnisFeature feature = runWithFeature.feature();
            updateFeature(feature, false);
        }
        super.finished(description);
    }

    private void updateFeature(final IgnisFeature feature, final boolean active) {
        featureService.get().updateFeatureState(feature, active);
    }
}
