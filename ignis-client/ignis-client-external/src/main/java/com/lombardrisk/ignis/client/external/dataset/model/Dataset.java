package com.lombardrisk.ignis.client.external.dataset.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.lombardrisk.ignis.common.json.LocalDateDeserializer;
import com.lombardrisk.ignis.common.json.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dataset {

    private long id;
    private String name;
    private String predicate;
    private long recordsCount;
    private String table;
    private long tableId;
    private Long rowKeySeed;
    private Long validationJobId;
    private Long pipelineJobId;
    private Long pipelineInvocationId;
    private Long pipelineStepInvocationId;
    private Date createdTime;
    private Date lastUpdated;
    private String entityCode;
    private Date referenceDate;
    private Long runKey;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate localReferenceDate;

    private String validationStatus;
    private boolean hasRules;
}
