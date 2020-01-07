package com.lombardrisk.ignis.client.external.job.staging.request.v1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lombardrisk.ignis.client.external.job.staging.request.DataSource;
import com.lombardrisk.ignis.client.external.job.staging.request.DatasetMetadata;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Builder
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Deprecated
public class StagingItemRequest {

    @NotNull
    @Valid
    private final DataSource source;

    @NotNull
    @Valid
    private final DatasetMetadata dataset;

    @NotBlank
    @ApiModelProperty("The schema display name")
    private final String schema;

    @ApiModelProperty("When 'true', starts a validation job after the staging job finishes successfully")
    private final boolean autoValidate;

    public StagingItemRequest.StagingItemRequestBuilder copy() {
        return builder()
                .source(source)
                .dataset(dataset)
                .schema(schema)
                .autoValidate(autoValidate);
    }
}
