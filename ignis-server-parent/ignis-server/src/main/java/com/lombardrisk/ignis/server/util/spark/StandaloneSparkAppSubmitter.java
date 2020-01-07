package com.lombardrisk.ignis.server.util.spark;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.common.log.CorrelationId;
import com.lombardrisk.ignis.spark.api.JobRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.spark.SparkConf;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.lombardrisk.ignis.server.init.SparkLibsInitializer.SPARK_LIBS_DIR;
import static java.nio.file.Files.createDirectories;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.apache.commons.lang3.SystemUtils.JAVA_HOME;
import static org.apache.commons.lang3.SystemUtils.JAVA_IO_TMPDIR;

@Slf4j
public class StandaloneSparkAppSubmitter implements AppSubmitter {

    private static final String CLASSPATH_SEP = IS_OS_WINDOWS ? ";" : ":";
    private static final String DEFAULT_MAX_MEM_FLAG = "-Xmx512M";

    private final String sparkAppsLogDir;
    private final String sparkLogConfigFile;
    private final String keystoreFile;
    private final String sparkExtraJarsDir;
    private final JobRequestFileWriter jobRequestFileWriter;

    public StandaloneSparkAppSubmitter(
            final String sparkLogConfigFile,
            final String keystoreFile,
            final String sparkAppsLogDir,
            final String sparkExtraJarsDir,
            final JobRequestFileWriter jobRequestFileWriter) {
        this.sparkLogConfigFile = sparkLogConfigFile;
        this.sparkAppsLogDir = sparkAppsLogDir;
        this.keystoreFile = keystoreFile;
        this.sparkExtraJarsDir = sparkExtraJarsDir;
        this.jobRequestFileWriter = jobRequestFileWriter;
    }

    @Override
    public AppSession submit(final SparkSubmitOption sparkSubmitOption) {
        ProcessBuilder standaloneSparkAppBuilder = toSparkAppBuilder(sparkSubmitOption);

        StandaloneAppSession sparkAppSession = StandaloneAppSession.builder()
                .sparkAppBuilder(standaloneSparkAppBuilder)
                .build();

        log.info(
                "Started standalone {} spark job for service request [{}]",
                sparkSubmitOption.getJobType(),
                sparkSubmitOption.getJobExecutionId());

        return sparkAppSession;
    }

    private ProcessBuilder toSparkAppBuilder(final SparkSubmitOption sparkSubmitOption) {
        String java = Paths.get(JAVA_HOME, "bin", "java").toAbsolutePath().toString();
        String classPathArg = toClassPathArg(sparkSubmitOption);
        File logFile = toLogFile(sparkSubmitOption, sparkSubmitOption.getJobExecutionId());
        File jobRequestFile = writeJobRequestFile(sparkSubmitOption.getJob());

        List<String> args = new ArrayList<>();
        args.addAll(ImmutableList.of(
                java,
                "-server",
                DEFAULT_MAX_MEM_FLAG,
                "-Dlog4j.configuration=log4j.properties"));
        args.addAll(toSystemProperties(sparkSubmitOption));
        args.addAll(ImmutableList.of(
                "-cp", classPathArg,
                sparkSubmitOption.getClazz(),
                jobRequestFile.getAbsolutePath(),
                CorrelationId.getCorrelationId()));

        ProcessBuilder standaloneSparkAppBuilder = new ProcessBuilder()
                .inheritIO()
                .redirectErrorStream(true)
                .redirectOutput(logFile)
                .command(args);

        Set<String> envProps = standaloneSparkAppBuilder.environment()
                .entrySet().stream()
                .map(Objects::toString)
                .collect(toSet());

        log.debug("Standalone spark job env:\n$ {}", String.join(" ", envProps));
        log.debug("Start standalone spark job:\n$ {}", String.join(" ", standaloneSparkAppBuilder.command()));

        return standaloneSparkAppBuilder;
    }

    private String toClassPathArg(final SparkSubmitOption sparkSubmitOption) {
        String sparkConfigDir = Paths.get(sparkLogConfigFile).getParent().toAbsolutePath().toString();
        String sparkLibsArg = Paths.get(JAVA_IO_TMPDIR, SPARK_LIBS_DIR).toAbsolutePath().toString() + "/*";
        String extraClasspath = getExtraJarsClasspath(sparkSubmitOption.getExtraJars(), CLASSPATH_SEP);

        Path mainAppJar = Paths.get(sparkSubmitOption.getDriverJar()).toAbsolutePath();
        return sparkConfigDir + CLASSPATH_SEP
                + sparkLibsArg + CLASSPATH_SEP
                + extraClasspath + CLASSPATH_SEP
                + mainAppJar;
    }

    private File toLogFile(final SparkSubmitOption sparkSubmitOption, final long jobId) {
        try {
            Path logFile = Paths.get(sparkAppsLogDir).resolve(
                    jobId + "_" + sparkSubmitOption.getJobType().name().toLowerCase() + ".log");
            createDirectories(logFile.getParent());
            return logFile.toFile();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private File writeJobRequestFile(final JobRequest jobRequest) {
        try {
            return jobRequestFileWriter.writeJobFile(jobRequest);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<String> toSystemProperties(final SparkSubmitOption sparkSubmitOption) {
        SparkConf sparkConf = sparkSubmitOption.getSparkConf();

        sparkConf.set("keystore.file", keystoreFile);
        sparkConf.set("spark.driver.extraClassPath",
                prependToSparkClasspath(sparkConf, "spark.driver.extraClassPath", sparkSubmitOption.getExtraJars()));
        sparkConf.set("spark.executor.extraClassPath",
                prependToSparkClasspath(sparkConf, "spark.executor.extraClassPath", sparkSubmitOption.getExtraJars()));

        return Arrays.stream(sparkConf.getAll())
                .map(prop -> "-D" + prop._1 + "=" + prop._2)
                .collect(toList());
    }

    private String prependToSparkClasspath(
            final SparkConf sparkConf, final String property, final List<String> extraJars) {

        String extraClasspath = getExtraJarsClasspath(extraJars, ",");
        return sparkConf.contains(property) ? extraClasspath + "," + sparkConf.get(property) : extraClasspath;
    }

    private String getExtraJarsClasspath(final List<String> extraJars, final String separator) {
        if (CollectionUtils.isEmpty(extraJars)) {
            return "";
        }

        File extraJarsDir = new File(sparkExtraJarsDir);

        return extraJars.stream()
                .map(jar -> extraJarsDir.toPath().resolve(jar).toAbsolutePath())
                .map(Path::toString)
                .collect(Collectors.joining(separator));
    }
}
