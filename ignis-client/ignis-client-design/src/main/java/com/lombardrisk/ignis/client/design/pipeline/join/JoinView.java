package com.lombardrisk.ignis.client.design.pipeline.join;

import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Builder
@Data
public class JoinView {

    private final Long id;
    private final Long leftSchemaId;
    private final Long rightSchemaId;
    private final JoinType joinType;
    private final List<JoinFieldView> joinFields;
}
