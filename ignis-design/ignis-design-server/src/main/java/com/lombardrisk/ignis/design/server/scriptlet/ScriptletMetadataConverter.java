package com.lombardrisk.ignis.design.server.scriptlet;

import lombok.experimental.UtilityClass;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class ScriptletMetadataConverter {

    public static StructTypeFieldView toStructTypeFieldView(final StructField structField) {
        return StructTypeFieldView.builder()
                .field(structField.name())
                .type(toString(structField.dataType()))
                .build();
    }

    public static List<StructTypeFieldView> toStructTypeView(final StructType structType) {
        return Arrays.stream(structType.fields())
                .map(ScriptletMetadataConverter::toStructTypeFieldView)
                .collect(Collectors.toList());
    }

    public static Map<String, List<StructTypeFieldView>> toScriptletInputView(final Map<String, StructType> inputs) {
        return inputs.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> toStructTypeView(e.getValue())));
    }

    private static String toString(DataType dataType) {
        return dataType.toString().replace("Type", "");
    }
}
