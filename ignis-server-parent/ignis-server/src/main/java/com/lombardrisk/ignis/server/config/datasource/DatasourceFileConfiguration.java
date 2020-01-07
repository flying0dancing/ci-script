package com.lombardrisk.ignis.server.config.datasource;

import com.lombardrisk.ignis.server.job.staging.file.DataSourceService;
import com.lombardrisk.ignis.server.job.staging.file.ErrorFileService;

public interface DatasourceFileConfiguration {
    DataSourceService dataSourceService();

    ErrorFileService errorFileService();

    enum Source {
        LOCAL,
        S3
    }

    Source getDatasetFileSource();
}
