package com.lombardrisk.ignis.server.job.staging;

import com.lombardrisk.ignis.api.dataset.DatasetState;
import com.lombardrisk.ignis.server.job.staging.model.StagingDataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StagingDatasetRepository extends JpaRepository<StagingDataset, Long> {

    List<StagingDataset> findByServiceRequestId(@Param("serviceRequestId") long serviceRequestId);

    StagingDataset findByServiceRequestIdAndDatasetNameAndEntityCodeAndReferenceDate(
            @Param("serviceRequestId") long serviceRequestId,
            @Param("datasetName") String datasetName,
            @Param("entityCode") String entityCode,
            @Param("referenceDate") LocalDate referenceDate);

    List<StagingDataset> findByServiceRequestIdAndDatasetName(
            @Param("serviceRequestId") long serviceRequestId,
            @Param("datasetName") String datasetName);

    List<StagingDataset> findByStatus(@Param("datasetState") DatasetState datasetState);

    List<StagingDataset> findByDatasetId(long datasetId);
}
