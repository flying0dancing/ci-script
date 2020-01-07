package com.lombardrisk.ignis.client.external.job.staging.request.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lombardrisk.ignis.client.external.job.staging.request.DatasetMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StagingRequestV2 {

    private String name;

    @NotNull
    @Valid
    private DatasetMetadata metadata;

    @Valid
    @NotNull
    @Size(min = 1)
    private Set<StagingItemRequestV2> items;

    @Valid
    private Set<String> downstreamPipelines;
}
