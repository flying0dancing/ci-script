package com.lombardrisk.ignis.client.internal.feature;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FeatureView {
    private final String name;
    private final boolean active;
}
