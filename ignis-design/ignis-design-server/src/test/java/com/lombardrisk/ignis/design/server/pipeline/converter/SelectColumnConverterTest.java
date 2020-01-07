package com.lombardrisk.ignis.design.server.pipeline.converter;

import com.lombardrisk.ignis.design.field.DesignField;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Order;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Select;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Window;
import com.lombardrisk.ignis.pipeline.step.api.OrderSpec;
import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import com.lombardrisk.ignis.pipeline.step.api.WindowSpec;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;

import static com.google.common.collect.Sets.newHashSet;

public class SelectColumnConverterTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void toSelectColumn_WindowWithNoOrders_ReturnsSelectColumn() {
        Select select = Select.builder()
                .select("dense_rank()")
                .isWindow(true)
                .window(Window.builder()
                        .partitions(newHashSet("A"))
                        .build())
                .build();

        SelectColumn selectColumn = SelectColumnConverter.toSelectColumn(
                select, DesignField.Populated.intField("ranqor").build());

        soft.assertThat(selectColumn.getSelect())
                .isEqualTo("dense_rank()");
        soft.assertThat(selectColumn.getAs())
                .isEqualTo("ranqor");
        soft.assertThat(selectColumn.getOver())
                .isEqualTo(WindowSpec.builder()
                        .partitionBy(newHashSet("A"))
                        .orderBy(Collections.emptyList())
                        .build());
    }

    @Test
    public void toSelectColumn_WindowWithNoPartitions_ReturnsSelectColumn() {

        Select select = Select.builder()
                .select("dense_rank()")
                .isWindow(true)
                .window(Window.builder()
                        .orders(newHashSet(Order.builder()
                                .fieldName("B")
                                .direction(Order.Direction.DESC)
                                .build()))
                        .build())
                .build();

        SelectColumn selectColumn = SelectColumnConverter.toSelectColumn(
                select, DesignField.Populated.intField("ranqor").build());

        soft.assertThat(selectColumn.getSelect())
                .isEqualTo("dense_rank()");
        soft.assertThat(selectColumn.getAs())
                .isEqualTo("ranqor");
        soft.assertThat(selectColumn.getOver())
                .isEqualTo(WindowSpec.builder()
                        .orderBy(Collections.singletonList(OrderSpec.builder()
                                .column("B")
                                .direction(OrderSpec.Direction.DESC)
                                .build()))
                        .build());
    }
}
