package com.lombardrisk.ignis.spark.pipeline.mock;

import com.lombardrisk.ignis.spark.script.api.Scriptlet;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructType;

import java.util.Map;

public class CannotInstantiateScriptlet implements Scriptlet {

    public CannotInstantiateScriptlet(final String arg1) {
        // no-op
    }

    @Override
    public Dataset<Row> run(final Map<String, Dataset<Row>> inputs) {
        return null;
    }

    @Override
    public Map<String, StructType> inputTraits() {
        return null;
    }

    @Override
    public StructType outputTrait() {
        return null;
    }
}
