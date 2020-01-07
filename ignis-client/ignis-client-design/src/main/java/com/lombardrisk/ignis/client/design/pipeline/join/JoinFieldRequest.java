package com.lombardrisk.ignis.client.design.pipeline.join;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Builder
@Data
public class JoinFieldRequest {
    @NotNull
    private final Long leftFieldId;
    @NotNull
    private final Long rightFieldId;
}
