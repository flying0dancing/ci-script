package com.lombardrisk.ignis.spark.core.phoenix;

import org.apache.spark.sql.jdbc.JdbcType;
import org.apache.spark.sql.types.DataTypes;
import org.junit.Test;

import java.sql.Types;

import static org.assertj.core.api.Assertions.assertThat;

public class PhoenixJdbcDialectTest {

    @Test
    public void getJdbcType_String_returnsStringType() {
        assertThat(PhoenixJdbcDialect.getJdbcType(DataTypes.StringType))
                .hasValue(new JdbcType("VARCHAR", Types.VARCHAR));
    }

    @Test
    public void getJdbcType_Integer_returnsIntegerType() {
        assertThat(PhoenixJdbcDialect.getJdbcType(DataTypes.IntegerType))
                .hasValue(new JdbcType("INTEGER", Types.INTEGER));
    }

    @Test
    public void getJdbcType_Long_returnsBigIntType() {
        assertThat(PhoenixJdbcDialect.getJdbcType(DataTypes.LongType))
                .hasValue(new JdbcType("BIGINT", Types.BIGINT));
    }

    @Test
    public void getJdbcType_Boolean_returnsBooleanType() {
        assertThat(PhoenixJdbcDialect.getJdbcType(DataTypes.BooleanType))
                .hasValue(new JdbcType("BOOLEAN", Types.BOOLEAN));
    }

    @Test
    public void getJdbcType_Decimal_returnsDecimalType() {
        assertThat(PhoenixJdbcDialect.getJdbcType(DataTypes.createDecimalType(2, 1)))
                .hasValue(new JdbcType(
                        String.format("DECIMAL(%d,%d)", 2, 1),
                        Types.DECIMAL));
    }

    @Test
    public void getJdbcType_Double_returnsDoubleType() {
        assertThat(PhoenixJdbcDialect.getJdbcType(DataTypes.DoubleType))
                .hasValue(new JdbcType("DOUBLE", Types.DOUBLE));
    }

    @Test
    public void getJdbcType_Date_returnsDateType() {
        assertThat(PhoenixJdbcDialect.getJdbcType(DataTypes.DateType))
                .hasValue(new JdbcType("DATE", Types.DATE));
    }

    @Test
    public void getJdbcType_Timestamp_returnsTimestampType() {
        assertThat(PhoenixJdbcDialect.getJdbcType(DataTypes.TimestampType))
                .hasValue(new JdbcType("TIMESTAMP", Types.TIMESTAMP));
    }

    @Test
    public void getJdbcType_Float_returnsFloatType() {
        assertThat(PhoenixJdbcDialect.getJdbcType(DataTypes.FloatType))
                .hasValue(new JdbcType("FLOAT", Types.FLOAT));
    }
}