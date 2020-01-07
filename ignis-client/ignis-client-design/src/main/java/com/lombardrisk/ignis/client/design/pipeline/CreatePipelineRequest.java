package com.lombardrisk.ignis.client.design.pipeline;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
@Builder
public class CreatePipelineRequest {

    @NotBlank
    private final String name;
    @NotNull
    private final Long productId;
}
