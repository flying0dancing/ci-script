package com.lombardrisk.ignis.client.external.pipeline.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SchemaReference {

    @NotEmpty
    private String physicalTableName;
    @NotEmpty
    private String displayName;
    @NotNull
    private Integer version;
}
