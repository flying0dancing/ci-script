package com.lombardrisk.ignis.server.job.staging.file;

import com.lombardrisk.ignis.server.job.staging.model.StagingDataset;
import com.lombardrisk.ignis.spark.api.staging.StagingErrorOutput;

import java.io.IOException;

public interface ErrorFileService {

    String errorFileRelativePath(final long serviceRequestId, final String physicalTableName);

    StagingErrorOutput errorOutput(StagingDataset stagingDataset);

    ErrorFileLinkOrStream downloadValidationErrorFile(final StagingDataset stagingDataset) throws IOException;
}
