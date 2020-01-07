package com.lombardrisk.ignis.spark.script.demo;

import com.lombardrisk.ignis.spark.script.api.Scriptlet;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.Map;

import static java.util.Collections.singletonMap;

@SuppressWarnings("squid:S1192")
public class AggregateUsersScriptlet implements Scriptlet {

    public static final StructType USERS_STRUCT_TYPE = new StructType(new StructField[]{
            new StructField("Username", DataTypes.StringType, false, Metadata.empty()),
            new StructField("Region", DataTypes.StringType, false, Metadata.empty())
    });

    public static final StructType USERS_BY_REGION_STRUCT_TYPE = new StructType(new StructField[] {
            new StructField("Region", DataTypes.StringType, false, Metadata.empty()),
            new StructField("UsersInRegion", DataTypes.IntegerType, false, Metadata.empty())
    });

    @Override
    public Dataset<Row> run(final Map<String, Dataset<Row>> inputs) {
        return inputs.get("Users").groupBy("Region")
                .agg(functions.expr("count(*)").as("UsersInRegion"));
    }

    @Override
    public Map<String, StructType> inputTraits() {
        return singletonMap("Users", USERS_STRUCT_TYPE);
    }

    @Override
    public StructType outputTrait() {
        return USERS_BY_REGION_STRUCT_TYPE;
    }
}
