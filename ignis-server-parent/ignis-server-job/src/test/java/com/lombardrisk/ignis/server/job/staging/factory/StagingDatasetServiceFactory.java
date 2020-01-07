package com.lombardrisk.ignis.server.job.staging.factory;

import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.server.dataset.DatasetService;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestRepository;
import com.lombardrisk.ignis.server.job.staging.JobStarter;
import com.lombardrisk.ignis.server.job.staging.StagingDatasetRepository;
import com.lombardrisk.ignis.server.job.staging.StagingDatasetService;
import com.lombardrisk.ignis.server.job.staging.StagingSparkConfigService;
import com.lombardrisk.ignis.server.job.staging.file.DataSourceService;
import com.lombardrisk.ignis.server.job.staging.file.ErrorFileService;
import com.lombardrisk.ignis.server.job.staging.validate.StagingRequestCommonValidator;
import com.lombardrisk.ignis.server.job.staging.validate.StagingRequestV1Validator;
import com.lombardrisk.ignis.server.job.staging.validate.StagingRequestV2Validator;
import com.lombardrisk.ignis.server.product.pipeline.PipelineService;
import com.lombardrisk.ignis.server.product.table.TableService;
import lombok.Builder;
import lombok.Getter;
import org.togglz.core.manager.FeatureManager;

@Getter
public class StagingDatasetServiceFactory {

    private final StagingDatasetService stagingDatasetService;

    @Builder
    public StagingDatasetServiceFactory(
            final TableService tableService,
            final DatasetService datasetService,
            final PipelineService pipelineService,
            final StagingDatasetRepository stagingDatasetRepository,
            final ServiceRequestRepository serviceRequestRepository,
            final JobStarter jobStarter,
            final TimeSource timeSource,
            final DataSourceService dataSourceService,
            final ErrorFileService errorFileService,
            final FeatureManager featureManager) {

        StagingRequestCommonValidator commonValidator = new StagingRequestCommonValidator(tableService);

        StagingRequestV1Validator stagingRequestV1Validator =
                new StagingRequestV1Validator(commonValidator);

        StagingRequestV2Validator stagingRequestV2Validator =
                new StagingRequestV2Validator(commonValidator, featureManager, datasetService, pipelineService);

        StagingSparkConfigService stagingSparkConfigService =
                new StagingSparkConfigService(dataSourceService, errorFileService);

        this.stagingDatasetService = new StagingDatasetService(
                stagingRequestV1Validator,
                stagingRequestV2Validator, stagingDatasetRepository,
                serviceRequestRepository,
                jobStarter,
                stagingSparkConfigService,
                timeSource,
                dataSourceService,
                errorFileService);
    }
}
