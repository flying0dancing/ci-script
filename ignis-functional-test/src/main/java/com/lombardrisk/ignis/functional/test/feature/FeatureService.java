package com.lombardrisk.ignis.functional.test.feature;

import com.lombardrisk.ignis.client.internal.FeaturesClient;
import com.lombardrisk.ignis.client.internal.feature.UpdateFeature;
import com.lombardrisk.ignis.feature.IgnisFeature;

import static com.lombardrisk.ignis.functional.test.steps.CallAssertion.callAndExpectSuccess;

public class FeatureService {

    private final FeaturesClient featuresClient;

    public FeatureService(final FeaturesClient featuresClient) {
        this.featuresClient = featuresClient;
    }

    public void updateFeatureState(final IgnisFeature feature, final boolean active) {
        callAndExpectSuccess(featuresClient.updateFeatureState(feature.name(), new UpdateFeature(active)));
    }
}
