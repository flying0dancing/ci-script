package com.lombardrisk.ignis.client.external.job.staging.request.v2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lombardrisk.ignis.client.external.job.staging.request.DataSource;
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
public class StagingItemRequestV2 {

    @NotNull
    @Valid
    private final DataSource source;

    @NotBlank
    @ApiModelProperty("The schema display name")
    private final String schema;

    @ApiModelProperty("When 'true', starts a validation job after the staging job finishes successfully")
    private final boolean autoValidate;

    @ApiModelProperty(value = "ID of existing dataset to append to", hidden = true)
    private final Long appendToDatasetId;

    public StagingItemRequestV2.StagingItemRequestV2Builder copy() {
        return builder()
                .source(source)
                .schema(schema)
                .autoValidate(autoValidate)
                .appendToDatasetId(appendToDatasetId);
    }
}
