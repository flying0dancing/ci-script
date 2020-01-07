package com.lombardrisk.ignis.server.job.staging.model;

import com.lombardrisk.ignis.api.dataset.DatasetState;
import com.lombardrisk.ignis.data.common.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.Date;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "STAGING_DATA_SET")
public class StagingDataset implements Identifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "START_TIME")
    private Date startTime;

    @Column(name = "END_TIME")
    private Date endTime;

    @Column(name = "MESSAGE")
    @Lob
    private String message;

    @Column(name = "STAGING_FILE")
    private String stagingFile;

    @Column(name = "STAGING_FILE_COPY")
    private String stagingFileCopy;

    @Column(name = "VALIDATION_ERROR_FILE")
    private String validationErrorFile;

    @Column(name = "TABLE_NAME")
    @NotNull
    private String table;

    @Column(name = "STATUS", length = 20)
    @Enumerated(EnumType.STRING)
    private DatasetState status;

    @Column(name = "LAST_UPDATE_TIME")
    private Date lastUpdateTime;

    @Column(name = "SERVICE_REQUEST_ID")
    private long serviceRequestId;

    @Column(name = "DATASET")
    @NotNull
    @Pattern(regexp = "^[a-zA-Z]+[\\w|_]*$")
    private String datasetName;

    @Column(name = "ENTITY_CODE")
    private String entityCode;

    @Column(name = "REFERENCE_DATE")
    private LocalDate referenceDate;

    @Column(name = "DATASET_ID")
    private Long datasetId;
}
