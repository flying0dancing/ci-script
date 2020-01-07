package com.lombardrisk.ignis.spark.script.api;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructType;

import java.util.Map;

public interface Scriptlet {

    Dataset<Row> run(Map<String, Dataset<Row>> inputs);

    Map<String, StructType> inputTraits();

    StructType outputTrait();
}
