package com.lombardrisk.ignis.functional.test.assertions.expected;

import com.lombardrisk.ignis.client.external.productconfig.view.FieldView;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Builder(builderMethodName = "expected")
@Getter
public class ExpectedPhysicalTable {

    private final String name;
    private final long numberOfRows;

    @Singular
    private final List<FieldView> columns;

    public List<String> getColumnNames() {
        return columns.stream().map(FieldView::getName).collect(toList());
    }

    public ExpectedPhysicalTable.ExpectedPhysicalTableBuilder copy() {
        return expected().name(this.name).numberOfRows(this.numberOfRows).columns(this.columns);
    }
}
