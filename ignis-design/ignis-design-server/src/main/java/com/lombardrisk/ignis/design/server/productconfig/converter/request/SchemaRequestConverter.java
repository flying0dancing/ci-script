package com.lombardrisk.ignis.design.server.productconfig.converter.request;

import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import com.lombardrisk.ignis.design.server.productconfig.schema.request.CreateSchemaRequest;
import io.vavr.Function2;

public class SchemaRequestConverter implements Function2<Long, CreateSchemaRequest, Schema> {

    private static final long serialVersionUID = 2009266316675675625L;

    @Override
    public Schema apply(final Long productId, final CreateSchemaRequest createSchemaRequest) {
        Schema schema = new Schema();
        schema.setProductId(productId);
        schema.setPhysicalTableName(createSchemaRequest.getPhysicalTableName());
        schema.setDisplayName(createSchemaRequest.getDisplayName());
        schema.setStartDate(createSchemaRequest.getStartDate());
        schema.setEndDate(createSchemaRequest.getEndDate());
        schema.setMajorVersion(createSchemaRequest.getMajorVersion());
        return schema;
    }
}
