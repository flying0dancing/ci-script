package com.lombardrisk.ignis.client.external.job.validation;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
@Builder
@ToString
public class ValidationJobRequest {

    @NotNull
    private final Long datasetId;
    @NotBlank
    private final String name;
}
