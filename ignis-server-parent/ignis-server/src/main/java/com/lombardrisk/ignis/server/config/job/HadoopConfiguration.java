package com.lombardrisk.ignis.server.config.job;

import com.lombardrisk.ignis.fs.FileSystemTemplate;
import com.lombardrisk.ignis.hadoop.HdfsTemplate;
import com.lombardrisk.ignis.hadoop.LocalFileSystemTemplate;
import com.lombardrisk.ignis.server.init.SparkLibsInitializer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PostConstruct;

@org.springframework.context.annotation.Configuration
public class HadoopConfiguration {

    private final String hadoopUser;
    private final String sparkLibsPath;
    private final boolean copySparkLibs;
    private final boolean localSpark;
    private final SparkDefaultsConfiguration sparkDefaultsConfiguration;
    private final HadoopSiteProperties hadoopSiteProperties;

    public HadoopConfiguration(
            @Value("${hadoop.user}") final String hadoopUser,
            @Value("${spark.libs.path}") final String sparkLibsPath,
            @Value("${spark.libs.copy}") final boolean copySparkLibs,
            @Value("${spark-defaults.conf.debug.mode}") final boolean localSpark,
            final SparkDefaultsConfiguration sparkDefaultsConfiguration,
            final HadoopSiteProperties hadoopSiteProperties) {
        this.hadoopUser = hadoopUser;
        this.sparkLibsPath = sparkLibsPath;
        this.copySparkLibs = copySparkLibs;
        this.localSpark = localSpark;
        this.sparkDefaultsConfiguration = sparkDefaultsConfiguration;
        this.hadoopSiteProperties = hadoopSiteProperties;
    }

    @PostConstruct
    public void setHadoopUserName() {
        System.setProperty("HADOOP_USER_NAME", hadoopUser);
    }

    @Bean
    public Configuration hadoopSiteConfiguration() {
        return hadoopSiteProperties.toConfiguration();
    }

    @Bean
    public FileSystemTemplate fileSystemTemplate() {
        if (localSpark) {
            return new LocalFileSystemTemplate();
        } else {
            return HdfsTemplate.builder()
                    .hdfsUser(hadoopUser)
                    .configuration(hadoopSiteConfiguration())
                    .build();
        }
    }

    @Bean
    @Lazy
    public YarnClient sparkYarnClient() {
        YarnClient yarnClient = YarnClient.createYarnClient();
        yarnClient.init(hadoopSiteConfiguration());
        yarnClient.start();
        return yarnClient;
    }

    @Bean
    public SparkLibsInitializer sparkLibsInitializer() {
        return new SparkLibsInitializer(
                sparkLibsPath,
                fileSystemTemplate(),
                sparkDefaultsConfiguration.sparkConfFactory(),
                localSpark,
                copySparkLibs);
    }
}
