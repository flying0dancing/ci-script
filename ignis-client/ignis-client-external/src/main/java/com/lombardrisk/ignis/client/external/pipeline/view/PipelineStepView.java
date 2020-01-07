package com.lombardrisk.ignis.client.external.pipeline.view;

import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PipelineStepView {

    private Long id;
    private String name;
    private SchemaDetailsView schemaIn;
    private SchemaDetailsView schemaOut;
    private List<String> groupings;
    private List<JoinView> joins;
    private List<SchemaDetailsView> unionInputSchemas;
    private String description;
    private TransformationType type;
    private String jarFile;
    private String className;
}
