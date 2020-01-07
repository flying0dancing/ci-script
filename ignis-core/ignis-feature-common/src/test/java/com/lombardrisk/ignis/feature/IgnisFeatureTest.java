package com.lombardrisk.ignis.feature;

import org.junit.Rule;
import org.junit.Test;
import org.togglz.junit.TogglzRule;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class IgnisFeatureTest {

    @Rule
    public final TogglzRule togglzRule = TogglzRule.allEnabled(IgnisFeature.class);

    @Test
    public void checkActive_FeatureInActive_ThrowsException() {
        togglzRule.disable(IgnisFeature.APPEND_DATASETS);

        assertThatThrownBy(IgnisFeature.APPEND_DATASETS::checkActive)
                .isInstanceOf(FeatureNotActiveException.class)
                .hasMessage("Feature APPEND_DATASETS is not active");
    }
}
