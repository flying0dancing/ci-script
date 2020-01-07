package com.lombardrisk.ignis.server.product.productconfig.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ProductManifest {
    private final String name;
    private final String version;
}
