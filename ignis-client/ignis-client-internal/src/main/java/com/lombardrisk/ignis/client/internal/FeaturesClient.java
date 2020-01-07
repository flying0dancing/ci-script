package com.lombardrisk.ignis.client.internal;

import com.lombardrisk.ignis.client.internal.feature.FeatureView;
import com.lombardrisk.ignis.client.internal.feature.UpdateFeature;
import com.lombardrisk.ignis.client.internal.path.api;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FeaturesClient {

    @Headers("Content-Type:application/json")
    @POST(api.internal.features.byName)
    Call<FeatureView> updateFeatureState(@Path(api.Params.NAME) String name, @Body UpdateFeature updateFeature);
}
