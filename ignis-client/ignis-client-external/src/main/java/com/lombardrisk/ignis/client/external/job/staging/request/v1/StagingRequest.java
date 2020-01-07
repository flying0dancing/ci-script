package com.lombardrisk.ignis.client.external.job.staging.request.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Deprecated
public class StagingRequest {

    private String name;

    @Valid
    @NotNull
    @Size(min = 1)
    private Set<StagingItemRequest> items;
}
