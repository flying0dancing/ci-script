# Ignis Spark Core
This module contains core classes that are used by both ignis-spark-staging and ignis-spark-validation.
These two sub modules compile to a runnable jar that is sent to the 
[Yarn](https://hadoop.apache.org/docs/current/hadoop-yarn/hadoop-yarn-site/YARN.html) cluster to stage or transform
data.

##Libraries
- Java 8
- [Spring Boot](https://spring.io/projects/spring-boot) - Used to manage beans/run the application
- [Apache Spark](https://spark.apache.org/) - Big Data technology to process rows of data
- [ApachePhoenix](https://phoenix.apache.org/Phoenix-in-15-minutes-or-less.html) - SQL Wrapper for [Apache Hbase](https://hbase.apache.org/)
- [Togglz](https://www.togglz.org/) - A feature toggle library for controlling features 

##Responsibilities
This module contains common code of the following type
- Data access
- Common Job Property config
- Internal client configuration

### Data access
The `com.lombardrisk.ignis.spark.config.ApplicationConfig` creates the `Spark Session` with a common configuraton. This 
Spark Session is used create, manage and map `Datasets`. 

Apache Phoenix classes are used to retireve and persist data. The `PhoenixDatasetRepository` handles vanilla Datasets whilst
the `PhoenixRowKeyDatasetRepository` handles Datasets that requrie a RowKey, these tables are the tables created by the product
developers.

#### Dataset Table Schema
The `com.lombardrisk.ignis.spark.core.schema.DatasetTableSchema` class contains all the information for creating schemas in the system.

There are two types of Phoenix tables in the FCR system
- System Tables (such as `VALIDATION_RULE_RESULTS`)
- Ad Hoc tables (tables created by Product Developer's schemas)

##### System Tables
These tables are created once are should not be altered.
We need to define a DatasetTableSchema for these as the StructType will be used to validate the Dataset transformations

In the example below from ValidationService the `validationResultsTableSchema` definition of columns is used to select
the correct data from the input dataset.
```
        Dataset<Row> newDataFrame = dataset
                .map(toValidationResult(validationRule), createResultEncoder(dataset))
                .map(toValidationResultRow(), createResultRowEncoder(dataset))
                .withColumnRenamed(ROW_KEY.name(), DATASET_ROW_KEY)
                .withColumn(DATASET_ID, functions.lit(datasetId))
                .withColumn(VALIDATION_RULE_ID, functions.lit(validationRule.getId()))
                .select(validationResultsTableSchema.getColumns()`
```

##### Ad Hoc Tables (ignis.api.Dataset)
These tables are created by the ignis-spark-staging job and have an explicit ROW_KEY that is added to the Spark Dataset
by `PhoenixRowKeyedDatasetRepository`

```
@Override
protected Dataset<Row> saveDataFrame(final Dataset<Row> dataFrame, final DatasetMetadata datasetMetadata) {
    Dataset<Row> dfWithRowKey = new PhoenixRowKeyDataFrame(ctx.getSparkSession(), dataFrame)
            .withRowKey(ctx.getJobExecutionId());

    return delegate.writeDataFrame(
            dfWithRowKey,
            DatasetTableSchema.rowKeyRangedSchema(datasetMetadata.getName(), dataFrame.schema()))
            .getOrElseThrow(errorMessage -> new DriverException(errorMessage.getMessage()));
}
```

#### Phoenix Row Key
The `PhoenixTableRowKey` class is used to chunk out a range of rows in the Phoenix database in which the Dataset can be saved,
this is done by provide a "seed" for the range. This seed takes the form of the ignis-server's Spring Batch JobExecutionId
which will be auto-incremented for every job and dataset.

##### BitShifting
The seed is bit shifted by 32 bits to create a block of ~4,000,000,000 rows.

Running the following code
```
public static void main(final String... args) {
    for (long seed = 1; seed < 10; seed++) {
        PhoenixTableRowKey rowKey = PhoenixTableRowKey.of(seed);
        Tuple2<Long, Long> range = rowKey.getRowKeyRange();
        System.out.println(String.format("( %s , %s )", range._1(), range._2()));
    }
}
```

Outputs the following result, we can see below that for each seed we block out a chunk of 4,294,967,296
```
( 4294967296 , 8589934591 )
( 8589934592 , 12884901887 )
( 12884901888 , 17179869183 )
( 17179869184 , 21474836479 )
( 21474836480 , 25769803775 )
( 25769803776 , 30064771071 )
( 30064771072 , 34359738367 )
( 34359738368 , 38654705663 )
( 38654705664 , 42949672959 )
```

This chunking will allow the creation of `Long.MAX_VALUE / 4294967296 = 2,147,483,647` datasets before we have issues 
with hitting MAX_LONG