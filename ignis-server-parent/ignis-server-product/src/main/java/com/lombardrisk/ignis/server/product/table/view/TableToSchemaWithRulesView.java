package com.lombardrisk.ignis.server.product.table.view;

import com.lombardrisk.ignis.client.external.productconfig.view.SchemaView;
import com.lombardrisk.ignis.server.product.rule.view.ValidationRuleToValidationRuleView;
import com.lombardrisk.ignis.server.product.table.model.Table;

import java.util.function.Function;
import java.util.stream.Collectors;

public class TableToSchemaWithRulesView implements Function<Table, SchemaView> {

    private final ValidationRuleToValidationRuleView toValidationRuleView = new ValidationRuleToValidationRuleView();

    @Override
    public SchemaView apply(final Table table) {

        return SchemaView.builder()
                .id(table.getId())
                .displayName(table.getDisplayName())
                .physicalTableName(table.getPhysicalTableName())
                .version(table.getVersion())
                .createdBy(table.getCreatedBy())
                .createdTime(table.getCreatedTime())
                .startDate(table.getStartDate())
                .endDate(table.getEndDate())
                .hasDatasets(table.getHasDatasets())
                .validationRules(table.getValidationRules().stream()
                        .map(toValidationRuleView)
                        .collect(Collectors.toSet()))
                .build();
    }
}
