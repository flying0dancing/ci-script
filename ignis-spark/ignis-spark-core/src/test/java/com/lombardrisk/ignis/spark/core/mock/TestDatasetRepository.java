package com.lombardrisk.ignis.spark.core.mock;

import com.lombardrisk.ignis.spark.core.repository.DatasetRepository;

import java.sql.SQLException;

public interface TestDatasetRepository extends DatasetRepository {

    void dropTable(String tableName) throws SQLException;

}
