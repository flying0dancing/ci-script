package com.lombardrisk.ignis.functional.test.assertions;

import com.lombardrisk.ignis.client.external.productconfig.view.SchemaView;
import com.lombardrisk.ignis.functional.test.assertions.expected.ExpectedPhysicalTable;
import org.assertj.core.api.SoftAssertions;

public final class SchemaViewAssertions {

    private final SoftAssertions soft = new SoftAssertions();

    public void assertSchema(final SchemaView actualSchemaView, final ExpectedPhysicalTable expectedSchema) {
        soft.assertThat(actualSchemaView.getPhysicalTableName())
                .describedAs("Schema with name [%s]")
                .startsWith(expectedSchema.getName());

        soft.assertAll();
    }
}
