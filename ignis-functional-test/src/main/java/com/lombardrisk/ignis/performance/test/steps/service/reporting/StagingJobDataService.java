package com.lombardrisk.ignis.performance.test.steps.service.reporting;

import com.lombardrisk.ignis.client.external.dataset.model.Dataset;
import com.lombardrisk.ignis.client.external.job.JobExecutionView;
import com.lombardrisk.ignis.client.external.job.JobsClient;
import com.lombardrisk.ignis.functional.test.config.properties.EnvironmentSystemProperties;
import com.lombardrisk.ignis.functional.test.dsl.StagingJobContext;
import com.lombardrisk.ignis.hadoop.HadoopFSMetrics;
import com.lombardrisk.ignis.hadoop.HadoopMetricsClient;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import static com.lombardrisk.ignis.functional.test.steps.CallAssertion.callAndExpectSuccess;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Slf4j
public class StagingJobDataService {
    private final EnvironmentSystemProperties environmentSystemProperties;
    private final JobsClient jobsClient;
    private final HadoopMetricsClient hadoopMetricsClient;

    public StagingJobDataService(
            final EnvironmentSystemProperties environmentSystemProperties,
            final JobsClient jobsClient,
            final HadoopMetricsClient hadoopMetricsClient) {
        this.environmentSystemProperties = environmentSystemProperties;
        this.jobsClient = jobsClient;
        this.hadoopMetricsClient = hadoopMetricsClient;
    }

    public List<StagingJobData> recordStagingJob(final Supplier<StagingJobContext> stagingJob) {
        HadoopFSMetrics hdfsMetricsBeforeJob = getHadoopFSMetrics();

        StagingJobContext stagingJobContext = stagingJob.get();
        Long stagingJobId = stagingJobContext.getJobId();

        HadoopFSMetrics hdfsMetricsAfterJob = getHadoopFSMetrics();

        if (stagingJobContext.isSuccess()) {
            return stagingJobContext.getStagedDatasets().stream()
                    .map(dataset -> buildData(stagingJobId, dataset, hdfsMetricsBeforeJob, hdfsMetricsAfterJob))
                    .collect(toList());
        }

        return emptyList();
    }

    private StagingJobData buildData(
            final long stagingJobId,
            final Dataset dataset,
            final HadoopFSMetrics hdfsMetricsBeforeJob,
            final HadoopFSMetrics hdfsMetricsAfterJob) {

        JobExecutionView stagingJob = callAndExpectSuccess(jobsClient.getJob(stagingJobId));

        return StagingJobData.builder()
                .datasetId(dataset.getId())
                .stagingJobId(stagingJobId)
                .datasetRows(dataset.getRecordsCount())
                .jobStartTime(Date.from(stagingJob.getStartTime().toInstant()))
                .jobFinishTime(Date.from(stagingJob.getEndTime().toInstant()))
                .duration(calculateJobDurationInSeconds(stagingJob))
                .sparkExecutoryMemory(environmentSystemProperties.getSparkExecutoryMemory())
                .sparkExecutorCores(environmentSystemProperties.getSparkExecutorCores())
                .sparkExecutorInstances(environmentSystemProperties.getSparkExecutorInstances())
                .sparkDriverMemory(environmentSystemProperties.getSparkDriverMemory())
                .sparkDefaultParallelism(environmentSystemProperties.getSparkDefaultParallelism())
                .hbaseHRegionMemstoreFlushSize(environmentSystemProperties.getHbaseHregionMemstoreFlushSize())
                .hbaseHeapsize(environmentSystemProperties.getHbaseHeapsize())
                .hbaseOffHeapSize(environmentSystemProperties.getHbaseOffheapsize())
                .hbaseBucketCacheSize(environmentSystemProperties.getHbaseBucketcacheSize())
                .hbaseClientWriteBuffer(environmentSystemProperties.getHbaseClientWriteBuffer())
                .hbaseRegionServerHandlerCount(environmentSystemProperties.getHbaseRegionserverHandlerCount())
                .yarnNodeManagerResourceMemoryMb(environmentSystemProperties.getYarnNodemanagerResourceMemoryMb())
                .yarnNodeManagerResourceCpuVcores(environmentSystemProperties.getYarnNodemanagerResourceCpuVcores())
                .hdfsCapacityUsedMBBefore(hdfsMetricsBeforeJob.getCapacityUsedMB())
                .hdfsCapacityRemainingMBBefore(hdfsMetricsBeforeJob.getCapacityRemainingMB())
                .hdfsCapacityUsedMBAfter(hdfsMetricsAfterJob.getCapacityUsedMB())
                .hdfsCapacityRemainingMBAfter(hdfsMetricsAfterJob.getCapacityRemainingMB())
                .hdfsCapacityUsedDifference(hdfsMetricsAfterJob.getCapacityUsedMB() - hdfsMetricsBeforeJob.getCapacityUsedMB())
                .hdfsCapacityRemainingDifference(hdfsMetricsAfterJob.getCapacityRemainingMB() - hdfsMetricsBeforeJob.getCapacityRemainingMB())
                .hdfsCapacityTotalMB(hdfsMetricsAfterJob.getCapacityTotalMB())
                .build();
    }

    private long calculateJobDurationInSeconds(final JobExecutionView stagingJob) {
        LocalDateTime startTime =
                stagingJob.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime finishTime =
                stagingJob.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        return Duration.between(startTime, finishTime).getSeconds();
    }

    private HadoopFSMetrics getHadoopFSMetrics() {
        return hadoopMetricsClient.getHadoopFSMetrics()
                .onFailure(throwable ->
                        log.warn("Failed to get HDFS metrics from Hadoop NameNode, returning defaults", throwable))
                .getOrElse(new HadoopFSMetrics());
    }
}
