package com.lombardrisk.ignis.client.external.pipeline.export.step.select;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class WindowExport {

    private Set<String> partitionBy;
    private List<OrderExport> orderBy;
}
