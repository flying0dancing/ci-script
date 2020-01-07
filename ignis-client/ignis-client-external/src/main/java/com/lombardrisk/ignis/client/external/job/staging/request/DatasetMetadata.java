package com.lombardrisk.ignis.client.external.job.staging.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
@Builder
@Data
public class DatasetMetadata {

    @NotBlank
    private final String entityCode;

    @NotBlank
    @ApiModelProperty(
            value = "Reference date for the Dataset in format dd/MM/yyyy", required = true, example = "31/12/2018")
    private final String referenceDate;
}
