package com.lombardrisk.ignis.server.util.spark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.lombardrisk.ignis.common.log.CorrelationId;
import com.lombardrisk.ignis.spark.api.JsonCodec;
import com.lombardrisk.ignis.web.common.config.TogglzConfiguration;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.spark.SparkConf;
import org.apache.spark.deploy.yarn.Client;
import org.apache.spark.deploy.yarn.ClientArguments;
import org.springframework.core.env.Environment;
import scala.Tuple2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.ArrayUtils.EMPTY_STRING_ARRAY;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.hadoop.security.UserGroupInformation.createRemoteUser;

@Slf4j
public class YarnSparkAppSubmitter implements AppSubmitter {

    private static final String PADDING_SEP = lineSeparator() + "    ";
    private static final String LOGGED_PASSWORD = "******";
    private static final String BLANK_PASSWORD = "<blank password>";

    private Client testClient;
    private final Configuration hadoopConfiguration;
    private final ObjectMapper objectMapper;
    private final JobRequestFileWriter jobRequestFileWriter;

    private final TogglzConfiguration togglzConfiguration;
    private final Environment environment;

    public YarnSparkAppSubmitter(
            final Configuration hadoopConfiguration,
            final ObjectMapper objectMapper,
            final JobRequestFileWriter jobRequestFileWriter,
            final TogglzConfiguration togglzConfiguration,
            final Environment environment) {
        this.hadoopConfiguration = hadoopConfiguration;
        this.objectMapper = objectMapper;
        this.jobRequestFileWriter = jobRequestFileWriter;
        this.togglzConfiguration = togglzConfiguration;
        this.environment = environment;
    }

    @VisibleForTesting
    YarnSparkAppSubmitter(
            final Client testClient,
            final ObjectMapper objectMapper,
            final JobRequestFileWriter jobRequestFileWriter,
            final TogglzConfiguration togglzConfiguration,
            final Environment environment) {

        this.testClient = testClient;
        this.hadoopConfiguration = new Configuration();
        this.objectMapper = objectMapper;
        this.jobRequestFileWriter = jobRequestFileWriter;
        this.togglzConfiguration = togglzConfiguration;
        this.environment = environment;
    }

    @Override
    public AppSession submit(final SparkSubmitOption sparkSubmitOption) throws IOException {
        log.trace("Submitting spark application with options {}", sparkSubmitOption);

        File jobFile = jobRequestFileWriter.writeJobFile(sparkSubmitOption.getJob());
        Client client = createClient(testClient, sparkSubmitOption, jobFile);

        PrivilegedAction<ApplicationId> submitApplicationAction = client::submitApplication;
        ApplicationId applicationId =
                createRemoteUser(sparkSubmitOption.getHdfsUser())
                        .doAs(submitApplicationAction);

        return YarnAppSession.builder()
                .appId(applicationId.getClusterTimestamp(), applicationId.getId())
                .client(client)
                .build();
    }

    private Client createClient(
            final Client client,
            final SparkSubmitOption sparkSubmitOption,
            final File jobFile) {

        if (client != null) {
            return client;
        }
        List<String> args = Lists.newArrayList(
                "--jar", sparkSubmitOption.getDriverJar(),
                "--class", sparkSubmitOption.getClazz(),
                "--arg", jobFile.getName(),
                "--arg", CorrelationId.getCorrelationId());

        SparkConf sparkConf = sparkSubmitOption.getSparkConf();

        sparkConf.set("spark.app.name", sparkSubmitOption.getJobName());
        sparkConf.set("spark.yarn.dist.files", toStagingFilesProp(sparkSubmitOption, jobFile));
        setSparkConfClasspath(sparkConf, sparkSubmitOption);

        logSparkJob(args, sparkConf);

        ClientArguments arguments = new ClientArguments(args.toArray(EMPTY_STRING_ARRAY));
        PrivilegedAction<Client> clientPrivilegedAction =
                () -> new Client(arguments, hadoopConfiguration, sparkConf);

        return createRemoteUser(sparkSubmitOption.getHdfsUser())
                .doAs(clientPrivilegedAction);
    }

    private void logSparkJob(final List<String> args, final SparkConf sparkConf) {
        if (log.isTraceEnabled()) {
            String argLines = toArgsByLine(args);
            log.trace("Spark args: {}{}", PADDING_SEP, argLines);

            String sparkConfLines = toSparkConfByLine(sparkConf);
            log.trace("Spark conf settings: {}{}", PADDING_SEP, sparkConfLines);
        }
    }

    private String toArgsByLine(final List<String> args) {
        StringBuilder argsBuilder = new StringBuilder();

        for (int i = 0; i < args.size(); i++) {
            String arg = args.get(i);

            if (arg != null && isSecondArg(i)) {
                if (arg.startsWith("%7B%22")) {
                    String json = toPrettyPrintedJson(arg);
                    argsBuilder.append(json);
                } else {
                    argsBuilder.append(arg);
                }
                argsBuilder.append(PADDING_SEP);
            } else {
                argsBuilder.append(arg).append(SPACE);
            }
        }
        return argsBuilder.toString();
    }

    @SuppressWarnings({ "squid:S109", "findbugs:IM_BAD_CHECK_FOR_ODD" })
    private static boolean isSecondArg(final int i) {
        return i % 2 == 1;
    }

    private String toPrettyPrintedJson(final String jsonArg) {
        try {
            JsonCodec jsonCodec = new JsonCodec(objectMapper);
            Object json = MAPPER.readValue(jsonCodec.decode(jsonArg), Object.class);

            return MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(json);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private static String toSparkConfByLine(final SparkConf sparkConf) {
        return Arrays.stream(sparkConf.getAll())
                .map(YarnSparkAppSubmitter::hiddenPassword)
                .map(pair -> pair._1 + " = " + pair._2)
                .collect(joining(PADDING_SEP));
    }

    private static Tuple2<String, String> hiddenPassword(final Tuple2<String, String> property) {
        if (property._1.endsWith("password")) {
            if (isBlank(property._2)) {
                return Tuple2.apply(property._1, BLANK_PASSWORD);
            }
            return Tuple2.apply(property._1, LOGGED_PASSWORD);
        } else {
            return property;
        }
    }

    private String toStagingFilesProp(final SparkSubmitOption sparkSubmitOption, final File jobFile) {
        String stagingFiles = String.join(
                ",",
                togglzConfiguration.getFeaturePropertiesFile().getAbsolutePath(),
                environment.getRequiredProperty("server.ssl.key-store"),
                environment.getRequiredProperty("spark.log4j.file"),
                jobFile.getAbsolutePath());

        Option<String> extraJarsClasspath = getExtraJarsClasspath(sparkSubmitOption.getExtraJars());
        if (extraJarsClasspath.isDefined()) {
            return stagingFiles + "," + extraJarsClasspath.get();
        }
        return stagingFiles;
    }

    private Option<String> getExtraJarsClasspath(final List<String> extraJars) {
        if (isEmpty(extraJars)) {
            return Option.none();
        }

        File extraJarsDir = environment.getRequiredProperty("spark.extra.jars.dir", File.class);

        return Option.of(extraJars.stream()
                .map(jar -> extraJarsDir.toPath().resolve(jar).toAbsolutePath())
                .map(Path::toString)
                .collect(Collectors.joining(",")));
    }

    private void setSparkConfClasspath(final SparkConf sparkConf, final SparkSubmitOption sparkSubmitOption) {
        List<String> extraJars = sparkSubmitOption.getExtraJars();

        if (isNotEmpty(extraJars)) {
            String commaSeparatedJars = String.join(",", extraJars);

            sparkConf.set("spark.driver.extraClassPath",
                    prependToSparkClasspath(sparkConf, "spark.driver.extraClassPath", commaSeparatedJars));
            sparkConf.set("spark.executor.extraClassPath",
                    prependToSparkClasspath(sparkConf, "spark.executor.extraClassPath", commaSeparatedJars));
        }
    }

    private String prependToSparkClasspath(final SparkConf sparkConf, final String property, final String prepend) {
        return sparkConf.contains(property) ? prepend + ":" + sparkConf.get(property) : prepend;
    }
}
