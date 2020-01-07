package com.lombardrisk.ignis.design.server.pipeline.converter;

import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.design.field.model.BooleanField;
import com.lombardrisk.ignis.design.field.model.DateField;
import com.lombardrisk.ignis.design.field.model.DecimalField;
import com.lombardrisk.ignis.design.field.model.DoubleField;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.field.model.FloatField;
import com.lombardrisk.ignis.design.field.model.IntField;
import com.lombardrisk.ignis.design.field.model.LongField;
import com.lombardrisk.ignis.design.field.model.StringField;
import com.lombardrisk.ignis.design.field.model.TimestampField;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Order;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Select;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Window;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import com.lombardrisk.ignis.pipeline.step.api.OrderSpec;
import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import com.lombardrisk.ignis.pipeline.step.api.UnionSpec;
import com.lombardrisk.ignis.pipeline.step.api.WindowSpec;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;

import static com.lombardrisk.ignis.common.stream.CollectionUtils.orEmpty;

@UtilityClass
public class SelectColumnConverter {

    public static SelectColumn toSelectColumn(
            final Select select,
            final Field outputField) {

        String selectValue = getSelectValueBasedOnNull(select, outputField);

        if (select.isWindow() && select.getWindow() != null) {
            return SelectColumn.builder()
                    .select(selectValue)
                    .as(outputField.getName())
                    .intermediateResult(select.isIntermediate())
                    .over(toWindowSpec(select.getWindow()))
                    .build();
        }

        return SelectColumn.builder()
                .select(selectValue)
                .intermediateResult(select.isIntermediate())
                .as(outputField.getName())
                .build();
    }

    private String getSelectValueBasedOnNull(Select select, Field outputField) {
        if (select.getSelect().equalsIgnoreCase("NULL") && outputField.isNullable()) {
            if (outputField instanceof BooleanField) {
                return "CAST(NULL as BOOLEAN)";
            } else if (outputField instanceof DateField) {
                return "Cast(NULL as Date)";
            } else if (outputField instanceof DecimalField) {
                return String.format(
                        "CAST(NULL as DECIMAL(%s,%s))",
                        ((DecimalField) outputField).getPrecision(),
                        ((DecimalField) outputField).getScale());
            } else if (outputField instanceof DoubleField) {
                return "CAST(NULL as DOUBLE)";
            } else if (outputField instanceof FloatField) {
                return "CAST(NULL as FLOAT)";
            } else if (outputField instanceof IntField) {
                return "CAST(NULL as INTEGER)";
            } else if (outputField instanceof LongField) {
                return "CAST(NULL as LONG)";
            } else if (outputField instanceof StringField) {
                return "CAST(NULL as STRING)";
            } else if (outputField instanceof TimestampField) {
                return "CAST(NULL as TIMESTAMP)";
            } else {
                return "NULL";
            }
        } else {
            return select.getSelect();
        }
    }

    public static SelectColumn toUnionSelectColumn(
            final Select select,
            final Field outputField,
            final Schema inputSchema) {

        return SelectColumn.builder()
                .select(getSelectValueBasedOnNull(select, outputField))
                .as(outputField.getName())
                .intermediateResult(select.isIntermediate())
                .union(UnionSpec.builder()
                        .schemaId(inputSchema.getId())
                        .schemaInPhysicalName(inputSchema.getPhysicalTableName())
                        .build())
                .build();
    }

    public static WindowSpec toWindowSpec(final Window window) {
        return WindowSpec.builder()
                .partitionBy(window.getPartitions())
                .orderBy(MapperUtils.mapCollection(
                        orEmpty(window.getOrders()),
                        SelectColumnConverter::toOrderSpec,
                        ArrayList::new))
                .build();
    }

    public static OrderSpec toOrderSpec(final Order order) {
        String fieldName = order.getFieldName();
        OrderSpec.Direction direction = OrderSpec.Direction.valueOf(order.getDirection().toString());
        return OrderSpec.column(fieldName, direction);
    }
}
