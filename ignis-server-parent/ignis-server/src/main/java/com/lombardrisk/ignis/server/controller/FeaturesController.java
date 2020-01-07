package com.lombardrisk.ignis.server.controller;

import com.lombardrisk.ignis.client.internal.feature.UpdateFeature;
import com.lombardrisk.ignis.client.internal.feature.FeatureView;
import com.lombardrisk.ignis.client.internal.path.api;
import com.lombardrisk.ignis.feature.IgnisFeature;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.web.common.response.FcrResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@AllArgsConstructor
@RequestMapping(produces = APPLICATION_JSON_VALUE)
public class FeaturesController {

    private final boolean featureUpdatesEnabled;
    private final FeatureManager featureManager;

    @GetMapping(api.internal.Features)
    public List<FeatureView> getAll() {
        return Stream.of(IgnisFeature.values())
                .map(ignisFeature -> new FeatureView(ignisFeature.name(), featureManager.isActive(ignisFeature)))
                .collect(toList());
    }

    @PostMapping(api.internal.features.byName)
    public FcrResponse<FeatureView> updateFeature(
            @PathVariable(api.Params.NAME) final String name, @RequestBody final UpdateFeature featureUpdate) {
        if (!featureUpdatesEnabled) {
            return FcrResponse.internalServerError(Collections.singletonList(
                    ErrorResponse.valueOf("Updating feature toggles is not allowed", "NOT_ALLOWED")));
        }

        Optional<IgnisFeature> featureOptional = findFeature(name);
        if (!featureOptional.isPresent()) {
            return FcrResponse.crudFailure(CRUDFailure.cannotFind("feature")
                    .with("name", name)
                    .asFailure());
        }

        IgnisFeature feature = featureOptional.get();
        featureManager.setFeatureState(new FeatureState(feature, featureUpdate.getEnabled()));

        return FcrResponse.okResponse(new FeatureView(feature.name(), featureManager.isActive(feature)));
    }

    private Optional<IgnisFeature> findFeature(final String name) {
        return Arrays.stream(IgnisFeature.values())
                .filter(ignisFeature -> ignisFeature.name().equals(name))
                .findFirst();
    }
}
