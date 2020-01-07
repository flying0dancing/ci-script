package com.lombardrisk.ignis.client.external.productconfig.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ProductConfigExport {

    private Long id;
    private String name;
    private String version;
    @Singular
    private List<SchemaExport> tables;
    private String importStatus;

}
