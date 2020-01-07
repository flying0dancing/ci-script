package com.lombardrisk.ignis.server.batch.staging;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * properties for hold the data set configuration
 */
@ConfigurationProperties(prefix = "dataset.source.location")
@Getter
@Setter
public class HdfsDatasetConf {

    /**
     * the input source of dataset
     */
    private String localPath;

    /**
     * a root directory in hdfs that used to store datasets
     */
    private String remotePath;

    public String getRemotePath() {
        return remotePath.replace('\\', '/');
    }
}
