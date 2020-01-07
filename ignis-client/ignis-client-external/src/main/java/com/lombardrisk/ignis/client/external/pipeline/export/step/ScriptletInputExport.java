package com.lombardrisk.ignis.client.external.pipeline.export.step;

import com.lombardrisk.ignis.client.external.pipeline.export.SchemaReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ScriptletInputExport {

    @NotNull(message = "input name cannot be null")
    private String inputName;

    @NotNull(message = "input schema cannot be null")
    @Valid
    private SchemaReference inputSchema;
}
