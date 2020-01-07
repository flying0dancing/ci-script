package com.lombardrisk.ignis.spark.api.staging;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lombardrisk.ignis.spark.api.staging.field.FieldValidation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StagingSchemaValidation implements Serializable {

    private static final long serialVersionUID = 1744176151033946046L;


    private String displayName;
    private String physicalTableName;
    private Long schemaId;

    @JsonDeserialize(as = LinkedHashSet.class)
    private Set<FieldValidation> fields = new LinkedHashSet<>();
}
