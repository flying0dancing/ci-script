package com.lombardrisk.ignis.client.design.productconfig;

import com.lombardrisk.ignis.client.design.schema.SchemaDto;
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
public class ProductConfigDto {

    private Long id;
    private String name;
    private String version;
    private ImportStatus importStatus;
    @Singular
    private List<SchemaDto> schemas;

    public enum ImportStatus {
        SUCCESS,
        ERROR
    }
}
