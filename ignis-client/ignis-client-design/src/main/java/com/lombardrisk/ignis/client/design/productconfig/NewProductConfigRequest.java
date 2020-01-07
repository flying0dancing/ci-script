package com.lombardrisk.ignis.client.design.productconfig;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Builder
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
@Data
public class NewProductConfigRequest {

    @NotBlank
    private final String name;

    @NotBlank
    private final String version;
}
