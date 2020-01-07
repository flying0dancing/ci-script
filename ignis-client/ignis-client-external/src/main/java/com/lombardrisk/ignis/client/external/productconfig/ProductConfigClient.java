package com.lombardrisk.ignis.client.external.productconfig;

import com.lombardrisk.ignis.client.core.view.IdView;
import com.lombardrisk.ignis.client.external.path.api;
import com.lombardrisk.ignis.client.external.productconfig.view.ProductConfigView;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

import java.util.List;

public interface ProductConfigClient {

    @GET(api.external.v1.ProductConfigs)
    Call<List<ProductConfigView>> getProductConfigs();

    @GET(api.external.v1.productConfigs.ById)
    Call<ProductConfigView> getProductConfig(@Path(api.Params.ID) Long productConfigId);

    @POST(api.external.v1.productConfigs.File)
    @Multipart
    Call<IdView> importProductConfig(@Part MultipartBody.Part productConfigFile);

    @DELETE(api.external.v1.productConfigs.ById)
    Call<IdView> deleteProductConfig(@Path(api.Params.ID) Long productConfigId);
}
