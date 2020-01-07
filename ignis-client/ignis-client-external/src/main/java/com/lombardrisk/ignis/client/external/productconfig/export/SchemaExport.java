package com.lombardrisk.ignis.client.external.productconfig.export;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchemaExport {

    private Long id;
    private String physicalTableName;
    private String displayName;
    private Integer version;
    private Date createdTime;
    private Date startDate;
    private Date endDate;

    private SchemaPeriod period;
    private String createdBy;
    private boolean hasDatasets;
    private List<FieldExport> fields = new ArrayList<>();
    private List<ValidationRuleExport> validationRules = new ArrayList<>();
}
