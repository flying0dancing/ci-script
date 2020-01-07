package com.lombardrisk.ignis.functional.test.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lombardrisk.ignis.fs.FileSystemTemplate;
import com.lombardrisk.ignis.functional.test.config.properties.NameNodeProperties;
import com.lombardrisk.ignis.hadoop.HadoopMetricsClient;
import com.lombardrisk.ignis.hadoop.HdfsTemplate;
import com.lombardrisk.ignis.hadoop.LocalFileSystemTemplate;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import org.apache.hadoop.conf.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static org.apache.commons.lang3.StringUtils.isBlank;

@org.springframework.context.annotation.Configuration
@EnableConfigurationProperties(NameNodeProperties.class)
public class HadoopClientConfig {

    private final NameNodeProperties nameNodeProperties;

    @Autowired
    public HadoopClientConfig(final NameNodeProperties nameNodeProperties) {
        this.nameNodeProperties = nameNodeProperties;
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder().build();
    }

    @Bean
    public HadoopMetricsClient metricsClient() {
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("http")
                .host(nameNodeProperties.getHost())
                .port(nameNodeProperties.getHttpPort())
                .build();

        return new HadoopMetricsClient(okHttpClient(), httpUrl, new ObjectMapper());
    }

    @Bean
    public FileSystemTemplate fileSystemTemplate() {
        if (isBlank(nameNodeProperties.getUser())) {
            return new LocalFileSystemTemplate();
        }
        return HdfsTemplate.builder()
                .hdfsUser(nameNodeProperties.getUser())
                .configuration(configuration())
                .build();
    }

    @Bean
    public Configuration configuration() {
        Configuration configuration = new Configuration();
        configuration.set("fs.defaultFS", nameNodeProperties.getNameNodeUrl());
        return configuration;
    }
}
