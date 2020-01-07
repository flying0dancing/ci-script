package com.lombardrisk.ignis.performance.test.steps.service.reporting;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class StagingJobData {
    private Long datasetId;
    private Long stagingJobId;
    private Long datasetRows;
    private Date jobStartTime;
    private Date jobFinishTime;
    private long duration;
    private String sparkExecutoryMemory;
    private int sparkExecutorCores;
    private int sparkExecutorInstances;
    private String sparkDriverMemory;
    private int sparkDefaultParallelism;
    private long hbaseHRegionMemstoreFlushSize;
    private String hbaseHeapsize;
    private String hbaseOffHeapSize;
    private int hbaseBucketCacheSize;
    private long hbaseClientWriteBuffer;
    private int hbaseRegionServerHandlerCount;
    private long yarnNodeManagerResourceMemoryMb;
    private int yarnNodeManagerResourceCpuVcores;
    private long hdfsCapacityTotalMB;
    private long hdfsCapacityUsedMBBefore;
    private long hdfsCapacityRemainingMBBefore;
    private long hdfsCapacityUsedMBAfter;
    private long hdfsCapacityRemainingMBAfter;
    private long hdfsCapacityUsedDifference;
    private long hdfsCapacityRemainingDifference;
}
