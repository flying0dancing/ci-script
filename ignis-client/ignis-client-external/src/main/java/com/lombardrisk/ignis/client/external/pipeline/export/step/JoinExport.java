package com.lombardrisk.ignis.client.external.pipeline.export.step;

import com.lombardrisk.ignis.client.external.pipeline.export.SchemaReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class JoinExport {

    @NotNull(message = "left schema cannot be null")
    @Valid
    private SchemaReference left;

    @NotNull(message = "right schema cannot be null")
    @Valid
    private SchemaReference right;

    @NotEmpty
    private List<JoinFieldExport> joinFields;

    @NotNull(message = "join type cannot be null")
    private JoinType type;
}
