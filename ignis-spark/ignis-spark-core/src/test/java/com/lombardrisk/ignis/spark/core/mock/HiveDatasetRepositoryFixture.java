package com.lombardrisk.ignis.spark.core.mock;

import com.lombardrisk.ignis.spark.core.hive.HiveDatasetRepository;
import org.apache.spark.sql.SparkSession;

public class HiveDatasetRepositoryFixture extends HiveDatasetRepository implements TestDatasetRepository {

    public HiveDatasetRepositoryFixture(final SparkSession sparkSession) {
        super(sparkSession);
    }

    @Override
    public void dropTable(final String tableName) {
        sparkSession.sqlContext().dropTempTable(tableName);
    }
}
