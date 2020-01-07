package com.lombardrisk.ignis.spark.core.repository;

import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.common.lang.ErrorMessage;
import com.lombardrisk.ignis.spark.core.schema.DatasetTableSchema;
import io.vavr.control.Either;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public interface DatasetRepository {

    Either<ErrorMessage, Dataset<Row>> writeDataFrame(
            final Dataset<Row> dataFrame,
            final DatasetTableSchema phoenixTableSchema);

    Dataset<Row> readDataFrame(final DatasetTableLookup datasetTableLookup);
}
