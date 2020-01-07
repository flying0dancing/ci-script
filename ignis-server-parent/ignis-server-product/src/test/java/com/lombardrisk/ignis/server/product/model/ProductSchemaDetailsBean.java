package com.lombardrisk.ignis.server.product.model;

import com.lombardrisk.ignis.server.product.productconfig.model.ProductSchemaDetailsOnly;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductSchemaDetailsBean implements ProductSchemaDetailsOnly {

    private String schemaPhysicalName;
    private String schemaDisplayName;
    private int schemaVersion;
    private String productName;
    private String productVersion;
}
