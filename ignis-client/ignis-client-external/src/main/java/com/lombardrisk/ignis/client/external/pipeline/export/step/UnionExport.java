package com.lombardrisk.ignis.client.external.pipeline.export.step;

import com.lombardrisk.ignis.client.external.pipeline.export.SchemaReference;
import com.lombardrisk.ignis.client.external.pipeline.export.step.select.SelectExport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UnionExport {

    private SchemaReference unionInSchema;
    private List<SelectExport> selects;
    private List<String> filters;
}
