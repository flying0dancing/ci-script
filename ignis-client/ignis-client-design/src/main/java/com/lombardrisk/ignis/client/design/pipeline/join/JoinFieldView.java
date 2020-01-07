package com.lombardrisk.ignis.client.design.pipeline.join;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class JoinFieldView {

    private final Long id;
    private final Long leftFieldId;
    private final Long rightFieldId;
}
