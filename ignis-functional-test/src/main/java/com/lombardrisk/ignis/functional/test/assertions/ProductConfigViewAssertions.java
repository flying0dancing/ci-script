package com.lombardrisk.ignis.functional.test.assertions;

import com.lombardrisk.ignis.client.design.productconfig.ProductConfigDto;
import com.lombardrisk.ignis.client.design.schema.SchemaDto;
import com.lombardrisk.ignis.client.external.productconfig.view.ProductConfigView;
import com.lombardrisk.ignis.client.external.productconfig.view.SchemaView;
import org.assertj.core.api.SoftAssertions;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Objects.equal;
import static java.util.stream.Collectors.toList;

public final class ProductConfigViewAssertions {

    private final SoftAssertions soft = new SoftAssertions();

    public void assertProductConfigIdenticalWithDesign(
            final ProductConfigView importedProduct,
            final ProductConfigDto designedProduct,
            final List<SchemaDto> expectedSchemas) {
        soft.assertThat(importedProduct.getId())
                .isNotNull();
        soft.assertThat(importedProduct.getName())
                .isEqualTo(designedProduct.getName());
        soft.assertThat(importedProduct.getVersion())
                .isEqualTo(designedProduct.getVersion());

        assertSchemasMatchDesign(importedProduct.getSchemas(), designedProduct.getSchemas(), expectedSchemas);
        soft.assertAll();
    }

    private void assertSchemasMatchDesign(
            final List<SchemaView> importedSchemas,
            final List<SchemaDto> designedSchemas,
            final List<SchemaDto> expectedSchemas) {

        List<SchemaDto> expectedDesignedSchemas = designedSchemas.stream()
                .filter(designedSchema -> isExpected(designedSchema, expectedSchemas))
                .collect(toList());

        for (SchemaDto designedSchema : expectedDesignedSchemas) {
            Optional<SchemaView> optionalImportedSchema = importedSchemas.stream()
                    .filter(schemaView -> schemaView.getDisplayName().equals(designedSchema.getDisplayName()))
                    .filter(schemaView -> schemaView.getVersion().equals(designedSchema.getMajorVersion()))
                    .findFirst();
            soft.assertThat(optionalImportedSchema).isPresent();

            optionalImportedSchema.ifPresent(importedSchema ->
                    assertSchemaMatchesDesign(importedSchema, designedSchema));
        }
    }

    private static boolean isExpected(final SchemaDto designedSchema, final List<SchemaDto> expectedSchemas) {
        return expectedSchemas.stream()
                .anyMatch(expectedSchema ->
                        equal(expectedSchema.getPhysicalTableName(), designedSchema.getPhysicalTableName())
                                && equal(expectedSchema.getMajorVersion(), designedSchema.getMajorVersion()));
    }

    private void assertSchemaMatchesDesign(final SchemaView importedSchema, final SchemaDto designedSchema) {
        soft.assertThat(importedSchema.getDisplayName())
                .isEqualTo(designedSchema.getDisplayName());
        soft.assertThat(importedSchema.getPhysicalTableName())
                .isEqualTo(designedSchema.getPhysicalTableName());
        soft.assertThat(importedSchema.getVersion())
                .isEqualTo(designedSchema.getMajorVersion());

        soft.assertThat(importedSchema.getCreatedTime())
                .isInSameDayAs(designedSchema.getCreatedTime());
    }
}
