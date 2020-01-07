package com.lombardrisk.ignis.client.design.pipeline.select;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class UnionRequest {

    @NotNull
    @Valid
    private final List<@Valid SelectRequest> selects;
    private final List<String> filters;
}
