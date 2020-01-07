package com.lombardrisk.ignis.client.external.pipeline.export.step;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class JoinFieldExport {

    @NotNull(message = "left column cannot be null")
    private String leftColumn;
    @NotNull(message = "right column cannot be null")
    private String rightColumn;
}
