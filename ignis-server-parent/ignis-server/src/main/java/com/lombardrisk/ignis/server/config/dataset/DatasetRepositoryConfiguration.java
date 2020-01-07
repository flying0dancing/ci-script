package com.lombardrisk.ignis.server.config.dataset;

import com.lombardrisk.ignis.server.batch.SparkJobExecutor;
import com.lombardrisk.ignis.server.config.ApplicationConf;
import com.lombardrisk.ignis.server.config.job.HadoopConfiguration;
import com.lombardrisk.ignis.server.dataset.DatasetJpaRepository;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestRepository;
import com.lombardrisk.ignis.server.job.staging.StagingDatasetRepository;
import com.lombardrisk.ignis.server.util.spark.AppSubmitter;
import com.lombardrisk.ignis.server.util.spark.JobRequestFileWriter;
import com.lombardrisk.ignis.server.util.spark.StandaloneSparkAppSubmitter;
import com.lombardrisk.ignis.server.util.spark.YarnSparkAppSubmitter;
import com.lombardrisk.ignis.web.common.config.TogglzConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class DatasetRepositoryConfiguration {

    private final ApplicationConf applicationConf;
    private final DatasetJpaRepository datasetRepository;
    private final StagingDatasetRepository stagingDatasetRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final HadoopConfiguration hadoopConfiguration;
    private final Environment env;
    private final TogglzConfiguration togglzConfiguration;

    @Autowired
    public DatasetRepositoryConfiguration(
            final ApplicationConf applicationConf,
            final DatasetJpaRepository datasetRepository,
            final StagingDatasetRepository stagingDatasetRepository,
            final ServiceRequestRepository serviceRequestRepository,
            final HadoopConfiguration hadoopConfiguration,
            final Environment environment,
            final TogglzConfiguration togglzConfiguration) {
        this.applicationConf = applicationConf;
        this.datasetRepository = datasetRepository;
        this.stagingDatasetRepository = stagingDatasetRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.hadoopConfiguration = hadoopConfiguration;
        this.env = environment;
        this.togglzConfiguration = togglzConfiguration;
    }

    public DatasetJpaRepository datasetRepository() {
        return datasetRepository;
    }

    public StagingDatasetRepository stagingDatasetRepository() {
        return stagingDatasetRepository;
    }

    public ServiceRequestRepository serviceRequestRepository() {
        return serviceRequestRepository;
    }

    @Bean
    public SparkJobExecutor sparkJobExecutor() {
        return new SparkJobExecutor(
                hadoopConfiguration.sparkYarnClient(),
                serviceRequestRepository,
                env.getRequiredProperty("spark.yarn.app.tracking.resource.url"),
                appSubmitter());
    }

    @Bean
    public AppSubmitter appSubmitter() {
        if (env.getProperty("spark-defaults.conf.debug.mode", Boolean.class, false)) {
            return new StandaloneSparkAppSubmitter(
                    env.getRequiredProperty("spark.log4j.file"),
                    env.getRequiredProperty("spark-defaults.conf.keystore.file"),
                    env.getRequiredProperty("spark-defaults.conf.spark.history.fs.logDirectory"),
                    env.getRequiredProperty("spark.extra.jars.dir"),
                    jobRequestFileWriter());
        }
        return new YarnSparkAppSubmitter(
                hadoopConfiguration.hadoopSiteConfiguration(),
                applicationConf.objectMapper(),
                jobRequestFileWriter(),
                togglzConfiguration,
                env);
    }

    @Bean
    public JobRequestFileWriter jobRequestFileWriter() {
        return new JobRequestFileWriter(applicationConf.objectMapper(), applicationConf.tempDirectory());
    }
}
