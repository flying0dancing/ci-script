package com.lombardrisk.ignis.server.product.pipeline.view;

import com.lombardrisk.ignis.client.external.pipeline.view.SchemaDetailsView;
import com.lombardrisk.ignis.server.product.pipeline.details.SchemaDetails;

import java.util.function.Function;

public class SchemaDetailsToSchemaDetailsView implements Function<SchemaDetails, SchemaDetailsView> {

    @Override
    public SchemaDetailsView apply(final SchemaDetails schemaDetails) {
        return SchemaDetailsView.builder()
                .id(schemaDetails.getId())
                .displayName(schemaDetails.getDisplayName())
                .physicalTableName(schemaDetails.getPhysicalTableName())
                .version(schemaDetails.getVersion())
                .build();
    }
}
