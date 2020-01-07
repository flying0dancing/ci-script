package com.lombardrisk.ignis.client.external.job.staging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
@JsonIgnoreProperties(ignoreUnknown = true)
public class StagingSchemaView {

    private final String name;
    private final String physicalName;
}
