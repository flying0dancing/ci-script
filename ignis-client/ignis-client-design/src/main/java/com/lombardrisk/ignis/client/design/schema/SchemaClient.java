package com.lombardrisk.ignis.client.design.schema;

import com.lombardrisk.ignis.client.design.path.design;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SchemaClient {

    @POST(design.api.v1.productConfigs.productId.Schemas)
    Call<SchemaDto> createSchema(
            @Path(design.api.Params.PRODUCT_ID) Long productConfigId, @Body SchemaDto schema);

    @GET(design.api.v1.productConfigs.productId.schemas.ById)
    Call<SchemaDto> getSchema(
            @Path(design.api.Params.PRODUCT_ID) Long productConfigId,
            @Path(design.api.Params.SCHEMA_ID) Long schemaId);

    @POST(design.api.v1.productConfigs.productId.schemas.schemaId.Fields)
    Call<FieldDto> createField(
            @Path(design.api.Params.PRODUCT_ID) Long productConfigId,
            @Path(design.api.Params.SCHEMA_ID) Long schemaId,
            @Body FieldDto fieldDto);
}
