package com.lombardrisk.ignis.server.config.job;

import lombok.Data;
import org.apache.hadoop.conf.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Map.Entry;

@Data
@ConfigurationProperties(prefix = "hadoop.site")
public class HadoopSiteProperties {

    private Map<String, String> conf;

    public Configuration toConfiguration() {
        Configuration configuration = new Configuration();

        for (Entry<String, String> entry : conf.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().replace('\\', '/');

            configuration.set(key, value);
        }
        return configuration;
    }
}
