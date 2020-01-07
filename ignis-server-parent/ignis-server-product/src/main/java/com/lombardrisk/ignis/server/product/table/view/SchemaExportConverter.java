package com.lombardrisk.ignis.server.product.table.view;

import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.server.product.rule.view.ValidationRuleExportConverter;
import com.lombardrisk.ignis.server.product.table.model.Table;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.function.Function;

import static com.lombardrisk.ignis.common.MapperUtils.mapCollection;
import static com.lombardrisk.ignis.common.MapperUtils.mapSet;

@AllArgsConstructor
public class SchemaExportConverter implements Function<SchemaExport, Table> {

    private final FieldExportConverter fieldViewConverter;
    private final ValidationRuleExportConverter validationRuleViewConverter;
    private final TimeSource timeSource;

    @Override
    public Table apply(final SchemaExport schemaExport) {
        return Table.builder()
                .physicalTableName(schemaExport.getPhysicalTableName())
                .displayName(schemaExport.getDisplayName())
                .version(schemaExport.getVersion())
                .startDate(findStartDate(schemaExport))
                .endDate(findEndDate(schemaExport))
                .fields(mapCollection(schemaExport.getFields(), fieldViewConverter, LinkedHashSet::new))
                .validationRules(mapSet(schemaExport.getValidationRules(), validationRuleViewConverter))
                .build();
    }

    private LocalDate findStartDate(final SchemaExport schemaExport) {
        if (schemaExport.getPeriod() != null) {
            return schemaExport.getPeriod().getStartDate();
        }

        return timeSource.localDateFromDate(schemaExport.getStartDate());
    }

    private LocalDate findEndDate(final SchemaExport schemaExport) {
        if (schemaExport.getPeriod() != null) {
            return schemaExport.getPeriod().getEndDate();
        }

        if (schemaExport.getEndDate() == null) {
            return null;
        }
        return timeSource.localDateFromDate(schemaExport.getEndDate());
    }
}
