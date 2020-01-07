package com.lombardrisk.ignis.spark.core.hive;

import com.lombardrisk.ignis.common.lang.ErrorMessage;
import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.core.repository.DatasetRepository;
import com.lombardrisk.ignis.spark.core.schema.DatasetTableSchema;
import io.vavr.control.Either;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public abstract class HiveDatasetRepository implements DatasetRepository {

    protected SparkSession sparkSession;

    protected HiveDatasetRepository(final SparkSession sparkSession) {
        this.sparkSession = sparkSession;
    }

    @Override
    public Either<ErrorMessage, Dataset<Row>> writeDataFrame(
            final Dataset<Row> dataFrame, final DatasetTableSchema phoenixTableSchema) {

        SQLContext sqlContext = sparkSession.sqlContext();

        sqlContext.registerDataFrameAsTable(dataFrame, phoenixTableSchema.getTableName());

        return Either.right(dataFrame);
    }

    @Override
    public Dataset<Row> readDataFrame(final DatasetTableLookup datasetTableLookup) {
        return readDataset(datasetTableLookup.getPredicate(), datasetTableLookup.getDatasetName());
    }

    private Dataset<Row> readDataset(final String predicate, final String datasetName) {
        String whereClause = EMPTY;
        if (isNotBlank(predicate)) {
            whereClause = " where " + predicate;
        }
        return sparkSession.sql("select * from " + datasetName + whereClause);
    }
}
