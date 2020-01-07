package com.lombardrisk.ignis.server.util.spark;

import com.lombardrisk.ignis.spark.api.JobRequest;
import com.lombardrisk.ignis.spark.api.JobType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.spark.SparkConf;

import java.util.List;

@Builder
@Getter
@EqualsAndHashCode(exclude = { "sparkConf" })
@ToString
public class SparkSubmitOption {

    private long jobExecutionId;

    private JobRequest job;
    private final String jobName;

    private final String driverJar;
    private final List<String> extraJars;
    private final JobType jobType;
    private final String clazz;

    private final SparkConf sparkConf;
    private final String hdfsUser;
}
