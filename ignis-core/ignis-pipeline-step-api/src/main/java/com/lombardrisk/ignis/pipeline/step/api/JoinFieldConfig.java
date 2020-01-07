package com.lombardrisk.ignis.pipeline.step.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinFieldConfig {
    private String leftJoinField;
    private String rightJoinField;
}
