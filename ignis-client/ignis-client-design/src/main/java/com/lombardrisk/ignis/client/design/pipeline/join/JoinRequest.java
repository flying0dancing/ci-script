package com.lombardrisk.ignis.client.design.pipeline.join;

import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@AllArgsConstructor
@Builder
@Data
public class JoinRequest {

    private final Long id;
    @NotNull
    private final Long leftSchemaId;
    @NotNull
    private final Long rightSchemaId;
    @NotNull
    private final List<JoinFieldRequest> joinFields;
    @NotNull
    private final JoinType joinType;
}
