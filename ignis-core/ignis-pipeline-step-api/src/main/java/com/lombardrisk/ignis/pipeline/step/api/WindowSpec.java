package com.lombardrisk.ignis.pipeline.step.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WindowSpec {

    private Set<String> partitionBy;
    private List<OrderSpec> orderBy;
}
