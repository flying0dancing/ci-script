package com.lombardrisk.ignis.client.external.pipeline.export;

import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineStepExport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PipelineExport {

    @NotEmpty(message = "Pipeline name cannot be empty")
    private String name;

    //Validation is done by looping over steps
    private List<PipelineStepExport> steps;

}
