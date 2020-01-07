package com.lombardrisk.ignis.spark.core.hive;

import com.lombardrisk.ignis.client.internal.InternalDatasetClient;
import com.lombardrisk.ignis.spark.core.server.DatasetEntityRepository;
import org.apache.spark.sql.SparkSession;

public class InMemoryDatasetRepository extends InMemoryRowKeyedDatasetRepository {

    public InMemoryDatasetRepository(
            final InternalDatasetClient datasetClient,
            final SparkSession sparkSession,
            final DatasetEntityRepository datasetEntityRepository) {

        super(datasetClient, sparkSession, datasetEntityRepository);
    }
}
