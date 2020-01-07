package com.lombardrisk.ignis.server.dataset.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.lombardrisk.ignis.api.dataset.DatasetType;
import com.lombardrisk.ignis.api.dataset.ValidationStatus;
import com.lombardrisk.ignis.common.json.LocalDateDeserializer;
import com.lombardrisk.ignis.common.json.LocalDateSerializer;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.Nameable;
import com.lombardrisk.ignis.server.product.table.model.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.lombardrisk.ignis.api.dataset.ValidationStatus.NOT_VALIDATED;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@javax.persistence.Table(name = "DATASET")
public class Dataset implements Identifiable, Nameable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "TABLE_NAME")
    private String table;

    @ManyToOne
    @JoinColumn(name = "DATASET_SCHEMA_ID")
    @JsonIgnore
    private Table schema;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DATASET_TYPE")
    @Enumerated(EnumType.STRING)
    private DatasetType datasetType;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "DATASET_ID", referencedColumnName = "ID")
    private Set<DatasetServiceRequest> stagingJobs = new LinkedHashSet<>();

    @Column(name = "VALIDATION_JOB_ID")
    private Long latestValidationJobId;

    @Column(name = "PIPELINE_JOB_ID")
    private Long pipelineJobId;

    @Column(name = "PIPELINE_INVOCATION_ID")
    private Long pipelineInvocationId;

    @Column(name = "PIPELINE_STEP_INVOCATION_ID")
    private Long pipelineStepInvocationId;

    @Column(name = "CREATED_TIME")
    private Date createdTime;

    @Column(name = "LAST_UPDATED")
    private Date lastUpdated;

    @Column(name = "RECORDS_COUNT")
    private Long recordsCount;

    @Column(name = "ROW_KEY_SEED")
    private Long rowKeySeed;

    @Column(name = "PREDICATE")
    private String predicate;

    @Column(name = "VALIDATION_STATUS")
    @Enumerated(EnumType.STRING)
    private ValidationStatus validationStatus = NOT_VALIDATED;

    @Column(name = "ENTITY_CODE")
    private String entityCode;

    @Column(name = "REFERENCE_DATE")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate referenceDate;

    @Column(name = "RUN_KEY")
    private Long runKey;

    public static SimpleDateFormat referenceDatFormat() {
        return new SimpleDateFormat("dd/MM/yyyy");
    }

    @SuppressWarnings("squid:S1068")
    public static class DatasetBuilder {

        private ValidationStatus validationStatus = NOT_VALIDATED;
        private Set<DatasetServiceRequest> stagingJobs = new LinkedHashSet<>();
    }
}
