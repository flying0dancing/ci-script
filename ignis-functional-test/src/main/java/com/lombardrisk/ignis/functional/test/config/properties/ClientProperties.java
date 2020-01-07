package com.lombardrisk.ignis.functional.test.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.nio.file.Path;

import static com.google.common.base.MoreObjects.toStringHelper;

@ConfigurationProperties(prefix = "client")
public class ClientProperties {

    private Path datasetsPath;
    private Path requestsPath;
    private Path downloadsPath;
    private long connectionTimeout;
    private long requestTimeout;

    @PostConstruct
    public void init() {
        DirectoryUtils.makeDirectory(downloadsPath);
    }

    public Path getDatasetsPath() {
        return datasetsPath;
    }

    public void setDatasetsPath(final Path datasetsPath) {
        this.datasetsPath = datasetsPath;
    }

    public Path getRequestsPath() {
        return requestsPath;
    }

    public void setRequestsPath(final Path requestsPath) {
        this.requestsPath = requestsPath;
    }

    public Path getDownloadsPath() {
        return downloadsPath;
    }

    public void setDownloadsPath(final Path downloadsPath) {
        this.downloadsPath = downloadsPath;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(final long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public long getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(final long requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("datasetsPath", datasetsPath)
                .add("requestsPath", requestsPath)
                .add("downloadsPath", downloadsPath)
                .add("connectionTimeout", connectionTimeout)
                .add("requestTimeout", requestTimeout)
                .toString();
    }
}
