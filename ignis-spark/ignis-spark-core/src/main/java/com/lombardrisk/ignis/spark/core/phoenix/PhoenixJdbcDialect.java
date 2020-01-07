package com.lombardrisk.ignis.spark.core.phoenix;

import com.lombardrisk.ignis.pipeline.step.api.DatasetFieldName;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.jdbc.JdbcType;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.DecimalType;
import org.apache.spark.sql.types.DecimalType$;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.MetadataBuilder;

import java.sql.Types;
import java.util.Optional;

@Slf4j
@UtilityClass
public class PhoenixJdbcDialect {

    public static final Metadata ROW_KEY_METADATA = new MetadataBuilder()
            .putBoolean(DatasetFieldName.ROW_KEY.getName(), true)
            .build();

    public static Optional<JdbcType> getJdbcType(DataType dataType) {
        if (dataType.simpleString().equals(DataTypes.FloatType.simpleString())) {
            return Optional.of(new JdbcType("FLOAT", Types.FLOAT));
        }
        if (dataType.simpleString().equals(DataTypes.IntegerType.simpleString())) {
            return Optional.of(new JdbcType("INTEGER", Types.INTEGER));
        }
        if (dataType.simpleString().equals(DataTypes.LongType.simpleString())) {
            return Optional.of(new JdbcType("BIGINT", Types.BIGINT));
        }
        if (dataType.simpleString().equals(DataTypes.BooleanType.simpleString())) {
            return Optional.of(new JdbcType("BOOLEAN", Types.BOOLEAN));
        }
        if (dataType.simpleString().startsWith(DecimalType$.MODULE$.simpleString())) {
            DecimalType decimalType = (DecimalType) dataType;
            return Optional.of(new JdbcType(
                    String.format("DECIMAL(%d,%d)", decimalType.precision(), decimalType.scale()),
                    Types.DECIMAL));
        }
        if (dataType.simpleString().equals(DataTypes.DoubleType.simpleString())) {
            return Optional.of(new JdbcType("DOUBLE", Types.DOUBLE));
        }
        if (dataType.simpleString().equals(DataTypes.StringType.simpleString())) {
            return Optional.of(new JdbcType("VARCHAR", Types.VARCHAR));
        }
        if (dataType.simpleString().equals(DataTypes.DateType.simpleString())) {
            return Optional.of(new JdbcType("DATE", Types.DATE));
        }
        if (dataType.simpleString().equals(DataTypes.TimestampType.simpleString())) {
            return Optional.of(new JdbcType("TIMESTAMP", Types.TIMESTAMP));
        }
        return Optional.empty();
    }
}
