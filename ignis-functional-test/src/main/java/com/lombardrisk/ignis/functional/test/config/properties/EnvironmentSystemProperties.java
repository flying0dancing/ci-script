package com.lombardrisk.ignis.functional.test.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
@Getter
@Setter
public class EnvironmentSystemProperties {

    @Value("${spark.executor.memory}")
    private String sparkExecutoryMemory;

    @Value("${spark.executor.cores}")
    private int sparkExecutorCores;

    @Value("${spark.executor.instances}")
    private int sparkExecutorInstances;

    @Value("${spark.driver.memory}")
    private String sparkDriverMemory;

    @Value("${spark.default.parallelism}")
    private int sparkDefaultParallelism;

    @Value("${hbase.hregion.memstore.flush.size}")
    private long hbaseHregionMemstoreFlushSize;

    @Value("${hbase.heapsize}")
    private String hbaseHeapsize;

    @Value("${hbase.offheapsize}")
    private String hbaseOffheapsize;

    @Value("${hbase.bucketcache.size}")
    private int hbaseBucketcacheSize;

    @Value("${hbase.client.write.buffer}")
    private long hbaseClientWriteBuffer;

    @Value("${hbase.regionserver.handler.count}")
    private int hbaseRegionserverHandlerCount;

    @Value("${yarn.nodemanager.resource.memory-mb}")
    private long yarnNodemanagerResourceMemoryMb;

    @Value("${yarn.nodemanager.resource.cpu-vcores}")
    private int yarnNodemanagerResourceCpuVcores;
}
