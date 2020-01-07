package com.lombardrisk.ignis.spark.core.phoenix;

import com.lombardrisk.ignis.common.lang.ErrorMessage;
import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.api.JobRequest;
import com.lombardrisk.ignis.spark.core.repository.DatasetRepository;
import com.lombardrisk.ignis.spark.core.schema.DatasetTableSchema;
import com.lombardrisk.ignis.spark.core.spark.SparkFunctions;
import com.lombardrisk.ignis.spark.util.ScalaOption;
import io.vavr.Tuple0;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.spark_project.guava.collect.Lists;
import scala.Some;
import scala.collection.JavaConversions;
import scala.collection.immutable.Map;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public class PhoenixDatasetRepository implements DatasetRepository {

    private final SparkSession sparkSession;
    private final JobRequest jobRequest;
    private final int saltBucketsSize;
    private final SparkFunctions sparkFunctions;
    private final String zookeeperUrl;

    public PhoenixDatasetRepository(
            final SparkSession sparkSession,
            final JobRequest jobRequest,
            final int saltBucketsSize,
            final SparkFunctions sparkFunctions,
            final String zookeeperUrl) {
        this.sparkSession = sparkSession;
        this.jobRequest = jobRequest;
        this.saltBucketsSize = saltBucketsSize;
        this.sparkFunctions = sparkFunctions;
        this.zookeeperUrl = zookeeperUrl;
    }

    public Either<ErrorMessage, Dataset<Row>> writeDataFrame(
            final Dataset<Row> dataFrame,
            final DatasetTableSchema phoenixTableSchema) {

        String tableName = phoenixTableSchema.getTableName();

        Try<Tuple0> trySave = Try.of(() -> saveDatasetToPhoenix(tableName, dataFrame));

        if (trySave.isFailure()) {
            log.error("Error occurred in saving dataset to phoenix", trySave.getCause());
            return Either.left(ErrorMessage.of(trySave.getCause()));
        }

        return Either.right(dataFrame);
    }

    public Dataset<Row> readDataFrame(final DatasetTableLookup datasetTableLookup) {
        log.debug("Reading dataframe for " + datasetTableLookup);
        return readDataFrame(
                datasetTableLookup.getDatasetName(), datasetTableLookup.getPredicate());
    }

    private Dataset<Row> readDataFrame(final String datasetName, final String predicate) {
        log.info("load phoenix table: {} with run key: {}", datasetName, jobRequest.getServiceRequestId());

        return sparkFunctions.sparkSqlFunctions()
                .phoenixTableAsDataFrame(
                        datasetName,
                        JavaConversions.asScalaBuffer(Lists.<String>newArrayList()).toSeq(),
                        new Some<>(predicate),
                        new Some<>(zookeeperUrl),
                        ScalaOption.NONE,
                        sparkSession.sparkContext().hadoopConfiguration()
                );
    }

    private Tuple0 saveDatasetToPhoenix(final String tableName, final Dataset<Row> dfWithRowKey) {
        log.debug("Storing into table [{}], {}", tableName, dfWithRowKey.schema());
        Map.Map2<String, String> map = new Map.Map2<>(
                "table", tableName,
                "zkUrl", zookeeperUrl
        );

        sparkFunctions.dataFrameFunctions(dfWithRowKey)
                .saveToPhoenix(map);

        return Tuple0.instance();
    }

    private void createTable(
            final DatasetTableSchema phoenixTableSchema,
            final String tableName,
            final Connection connection) throws SQLException {

        try (Statement statement = connection.createStatement()) {
            log.info("Create table [{}] with [{}] salted buckets", tableName, saltBucketsSize);

            statement.execute(phoenixTableSchema.createTableDDL(saltBucketsSize));
        }
    }
}
