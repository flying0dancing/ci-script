package com.lombardrisk.ignis.client.design;

import com.lombardrisk.ignis.client.external.productconfig.export.ValidationRuleExport;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

import static com.lombardrisk.ignis.client.design.path.design.api.Params;
import static com.lombardrisk.ignis.client.design.path.design.api.v1;

public interface RuleSetClient {

    @POST(v1.productConfigs.productId.schemas.schemaId.Rules)
    Call<ValidationRuleExport> saveRule(
            @Path(Params.PRODUCT_ID) final Long productIdId,
            @Path(Params.SCHEMA_ID) final Long tableId,
            @Body ValidationRuleRequest request);
}
