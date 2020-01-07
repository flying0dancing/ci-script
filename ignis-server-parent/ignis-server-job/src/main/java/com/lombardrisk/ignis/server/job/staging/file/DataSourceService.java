package com.lombardrisk.ignis.server.job.staging.file;

import com.lombardrisk.ignis.server.job.staging.model.StagingDataset;
import com.lombardrisk.ignis.spark.api.staging.datasource.DataSource;

import java.io.IOException;
import java.util.stream.Stream;

public interface DataSourceService {

    Stream<String> findFileNames() throws IOException;

    DataSource createSparkDataSource(final StagingDataset stagingDataset, final boolean header);

    /**
     * Location the datasource file is copied to, return null if no copy should take place
     *
     * @param serviceRequestId  Id of the service request for staging the data source
     * @param physicalTableName Name of the physical table in which the data should be saved
     * @return Location of staging file copy, null if not copied.
     */
    String stagingFileCopyLocation(long serviceRequestId, String physicalTableName);
}
