package com.lombardrisk.ignis.server.config.datasource;

import com.lombardrisk.ignis.fs.FileSystemTemplate;
import com.lombardrisk.ignis.server.batch.staging.HdfsDatasetConf;
import com.lombardrisk.ignis.server.batch.staging.file.LocalFileService;
import com.lombardrisk.ignis.server.batch.staging.file.error.HDFSErrorFileService;
import com.lombardrisk.ignis.server.job.staging.file.DataSourceService;
import com.lombardrisk.ignis.server.job.staging.file.ErrorFileService;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
@ConditionalOnProperty(name = "dataset.source.location.type", havingValue = "LOCAL")
public class LocalDataSourceConfiguration implements DatasourceFileConfiguration {

    private final HdfsDatasetConf datasetConf;
    private final FileSystemTemplate fileSystemTemplate;

    @Bean
    @Override
    public DataSourceService dataSourceService() {
        return new LocalFileService(datasetConf);
    }

    @Override
    public ErrorFileService errorFileService() {
        return new HDFSErrorFileService(datasetConf, fileSystemTemplate);
    }

    @Bean
    @Override
    public Source getDatasetFileSource() {
        return Source.LOCAL;
    }
}
