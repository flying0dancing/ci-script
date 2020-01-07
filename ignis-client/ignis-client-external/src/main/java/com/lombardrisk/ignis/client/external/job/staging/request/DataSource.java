package com.lombardrisk.ignis.client.external.job.staging.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Data
@Builder
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
public class DataSource {

    @NotBlank
    private final String filePath;
    private final boolean header;
}
