package com.lombardrisk.ignis.spark.staging;

import com.google.common.collect.ImmutableList;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.junit.Test;
import scala.Tuple2;

import java.io.File;
import java.util.Map;

public class SparkJobTest {

    @Test
    public void wordCount() {
        String target = new File("target").getAbsolutePath();
        String sparkWarehouse = target + "/spark-warehouse";
        String hiveMetaStore = "jdbc:derby:;databaseName=" + target + "/metastore_db;create=true";

        SparkSession sparkSession =
                SparkSession.builder()
                        .config("spark.sql.warehouse.dir", sparkWarehouse)
                        .config("javax.jdo.option.ConnectionURL", hiveMetaStore)
                        .master("local[*]")
                        .enableHiveSupport()
                        .getOrCreate();

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkSession.sparkContext());
        JavaRDD<String> javaRDD = javaSparkContext.parallelize(
                ImmutableList.of("hello", "hello", "world", "test"));

        Map<String, Long> stringLongMap =
                javaRDD.mapToPair(value -> new Tuple2<>(value, 1L))
                        .reduceByKey((v1, v2) -> v1 + v2)
                        .collectAsMap();
        // FIXME add assertions
    }
}
