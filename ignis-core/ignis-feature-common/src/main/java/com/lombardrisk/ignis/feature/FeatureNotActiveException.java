package com.lombardrisk.ignis.feature;

import lombok.Getter;

public class FeatureNotActiveException extends RuntimeException {

    private static final long serialVersionUID = -8922705019834433715L;
    private final IgnisFeature ignisFeature;

    public FeatureNotActiveException(final IgnisFeature ignisFeature) {
        this.ignisFeature = ignisFeature;
    }

    public IgnisFeature getFeature() {
        return ignisFeature;
    }

    @Override
    public String getMessage() {
        return String.format("Feature %s is not active", ignisFeature);
    }
}
