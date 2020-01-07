package com.lombardrisk.ignis.functional.test.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "name-node")
@Getter
@Setter
public class NameNodeProperties {
    private String user;
    private String host;
    private int httpPort;
    private int hdfsPort;

    public String getNameNodeUrl() {
        return String.format("hdfs://%s:%d", host, hdfsPort);
    }
}
