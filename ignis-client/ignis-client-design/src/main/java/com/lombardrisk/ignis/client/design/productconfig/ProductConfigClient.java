package com.lombardrisk.ignis.client.design.productconfig;

import com.lombardrisk.ignis.client.core.view.IdView;
import com.lombardrisk.ignis.client.design.path.design.api;
import com.lombardrisk.ignis.client.design.schema.NewSchemaVersionRequest;
import com.lombardrisk.ignis.client.design.schema.SchemaDto;
import com.lombardrisk.ignis.client.design.schema.UpdateSchema;
import com.lombardrisk.ignis.client.external.productconfig.export.ProductConfigExport;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.List;

public interface ProductConfigClient {

    @POST(api.v1.ProductConfigs)
    Call<ProductConfigDto> saveProductConfig(@Body NewProductConfigRequest productConfigRequest);

    @GET(api.v1.productConfigs.productId.File)
    Call<ResponseBody> exportProductConfig(@Path(api.Params.PRODUCT_ID) Long productConfigId);

    @DELETE(api.v1.productConfigs.ById)
    Call<ProductConfigExport> deleteProductConfig(@Path(api.Params.PRODUCT_ID) Long productConfigId);

    @GET(api.v1.ProductConfigs)
    Call<List<ProductConfigDto>> findAllProductConfigs();

    @POST(api.v1.productConfigs.productId.Schemas)
    Call<SchemaDto> addSchema(
            @Path(api.Params.PRODUCT_ID) final Long productId,
            @Body final SchemaDto newSchemaView);

    @PATCH(api.v1.productConfigs.ById + "?type=product")
    Call<ProductConfigDto> updateProduct(
            @Path(api.Params.PRODUCT_ID) final Long productId,
            @Body final UpdateProductConfig productUpdateRequest);

    @POST(api.v1.productConfigs.productId.schemas.ById)
    Call<SchemaDto> createNextVersion(
            @Path(api.Params.PRODUCT_ID) final Long productId,
            @Path(api.Params.SCHEMA_ID) final Long schemaId,
            @Body final NewSchemaVersionRequest newSchemaVersionRequest);

    @PATCH(api.v1.productConfigs.productId.schemas.ById)
    Call<IdView> editSchema(
            @Path(api.Params.PRODUCT_ID) final Long productId,
            @Path(api.Params.SCHEMA_ID) final Long id,
            @Body final UpdateSchema updatedSchemaView);
}
