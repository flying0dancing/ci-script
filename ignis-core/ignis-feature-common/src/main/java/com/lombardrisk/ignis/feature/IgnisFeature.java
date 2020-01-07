package com.lombardrisk.ignis.feature;

import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

public enum IgnisFeature implements Feature {

    @Label("ARCE-1017: Append Datasets")
    APPEND_DATASETS,

    @Label("ARCE-1110: Run Pipeline Step")
    RUN_PIPLINE_STEP;

    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }

    public void checkActive() {
        if (!isActive()) {
            throw new FeatureNotActiveException(this);
        }
    }
}
