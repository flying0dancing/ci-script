package com.lombardrisk.ignis.design.server.scriptlet;

import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ScriptletMetadataConverterTest {

    @Test
    public void toStructTypeFieldView_returnsStructTypeFieldViewObject() {
        assertThat(ScriptletMetadataConverter.toStructTypeFieldView(anyField("", DataTypes.StringType)))
                .isInstanceOf(StructTypeFieldView.class);
    }

    @Test
    public void toStructTypeFieldView_returnsWithFieldName() {
        final StructTypeFieldView convertedField =
                ScriptletMetadataConverter.toStructTypeFieldView(anyField("FiledName", DataTypes.StringType));
        assertThat(convertedField.getField()).isEqualTo("FiledName");
    }

    @Test
    public void toStructTypeFieldView_structFieldDataTypeString_returnsTypeString() {
        final StructTypeFieldView convertedField =
                ScriptletMetadataConverter.toStructTypeFieldView(anyField("any field name", DataTypes.StringType));
        assertThat(convertedField.getType()).isEqualTo("String");
    }

    @Test
    public void toStructTypeFieldView_structFieldDataTypeLong_returnsTypeLong() {
        final StructTypeFieldView convertedField =
                ScriptletMetadataConverter.toStructTypeFieldView(anyField("any field name", DataTypes.LongType));
        assertThat(convertedField.getType()).isEqualTo("Long");
    }

    @Test
    public void toStructTypeFieldView_structFieldDataTypeInteger_returnsTypeInteger() {
        final StructTypeFieldView convertedField =
                ScriptletMetadataConverter.toStructTypeFieldView(anyField("any field name", DataTypes.IntegerType));
        assertThat(convertedField.getType()).isEqualTo("Integer");
    }

    @Test
    public void toStructTypeFieldView_structFieldDataTypeDate_returnsTypeDate() {
        final StructTypeFieldView convertedField =
                ScriptletMetadataConverter.toStructTypeFieldView(anyField("any field name", DataTypes.DateType));
        assertThat(convertedField.getType()).isEqualTo("Date");
    }

    @Test
    public void toStructTypeFieldView_structFieldDataTypeBoolean_returnsTypeBoolean() {
        final StructTypeFieldView convertedField =
                ScriptletMetadataConverter.toStructTypeFieldView(anyField("any field name", DataTypes.BooleanType));
        assertThat(convertedField.getType()).isEqualTo("Boolean");
    }

    @Test
    public void toStructTypeFieldView_structFieldDataTypeDouble_returnsTypeDouble() {
        final StructTypeFieldView convertedField =
                ScriptletMetadataConverter.toStructTypeFieldView(anyField("any field name", DataTypes.DoubleType));
        assertThat(convertedField.getType()).isEqualTo("Double");
    }

    @Test
    public void toStructTypeFieldView_structFieldDataTypeFloat_returnsTypeFloat() {
        final StructTypeFieldView convertedField =
                ScriptletMetadataConverter.toStructTypeFieldView(anyField("any field name", DataTypes.FloatType));
        assertThat(convertedField.getType()).isEqualTo("Float");
    }

    @Test
    public void toStructTypeFieldView_structFieldDataTypeBinary_returnsTypeBinary() {
        final StructTypeFieldView convertedField =
                ScriptletMetadataConverter.toStructTypeFieldView(anyField("any field name", DataTypes.BinaryType));
        assertThat(convertedField.getType()).isEqualTo("Binary");
    }

    @Test
    public void toStructTypeFieldView_structFieldDataTypeTimestamp_returnsTypeTimestamp() {
        final StructTypeFieldView convertedField =
                ScriptletMetadataConverter.toStructTypeFieldView(anyField("any field name", DataTypes.TimestampType));
        assertThat(convertedField.getType()).isEqualTo("Timestamp");
    }

    @Test
    public void toStructTypeFieldView_structFieldDataTypeByte_returnsTypeByte() {
        final StructTypeFieldView convertedField =
                ScriptletMetadataConverter.toStructTypeFieldView(anyField("any field name", DataTypes.ByteType));
        assertThat(convertedField.getType()).isEqualTo("Byte");
    }

    @Test
    public void toStructTypeFieldView_structFieldDataTypeShort_returnsTypeShort() {
        final StructTypeFieldView convertedField =
                ScriptletMetadataConverter.toStructTypeFieldView(anyField("any field name", DataTypes.ShortType));
        assertThat(convertedField.getType()).isEqualTo("Short");
    }

    @Test
    public void toStructTypeFieldView_structFieldDataTypeNULL_returnsTypeNull() {
        final StructTypeFieldView convertedField =
                ScriptletMetadataConverter.toStructTypeFieldView(anyField("any field name", DataTypes.NullType));
        assertThat(convertedField.getType()).isEqualTo("Null");
    }

    @Test
    public void toStructTypeFieldView_structFieldDataTypeCalendarInterval_returnsTypeCalendarInterval() {
        final StructTypeFieldView convertedField =
                ScriptletMetadataConverter.toStructTypeFieldView(anyField(
                        "any field name",
                        DataTypes.CalendarIntervalType));
        assertThat(convertedField.getType()).isEqualTo("CalendarInterval");
    }

    @Test
    public void toStructTypeView_returnsListOfStructTypeFieldViewObjects() {
        StructType structType = new StructType(new StructField[]{
                anyField("FieldName", DataTypes.StringType)
        });

        List<StructTypeFieldView> result = ScriptletMetadataConverter.toStructTypeView(structType);
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0)).isEqualToComparingFieldByField(
                StructTypeFieldView.builder()
                        .field("FieldName")
                        .type("String").build());
    }

    @Test
    public void toScriptletInputView_returnsMapsWithConvertedStructType() {
        StructType structType = new StructType(new StructField[]{
                anyField("FieldName", DataTypes.StringType)
        });
        Map<String, StructType> inputSchemas = new HashMap<>();
        inputSchemas.put("InputSchemaName", structType);

        Map<String, List<StructTypeFieldView>> result = ScriptletMetadataConverter.toScriptletInputView(inputSchemas);
        assertThat(result.size()).isEqualTo(1);
        assertThat(result).containsKey("InputSchemaName");
        assertThat(result.get("InputSchemaName").size()).isEqualTo(1);

        assertThat(result.get("InputSchemaName").get(0)).isEqualToComparingFieldByField(
                StructTypeFieldView.builder()
                        .field("FieldName")
                        .type("String").build());
    }

    private StructField anyField(final String name, final DataType dataType) {
        return new StructField(name, dataType, false, Metadata.empty());
    }
}