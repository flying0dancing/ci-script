package com.lombardrisk.ignis.client.design.pipeline;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
@Builder
public class UpdatePipelineRequest {

    @NotBlank
    private final String name;
}
