package com.lombardrisk.ignis.server.product.productconfig;

import com.lombardrisk.ignis.client.external.productconfig.export.SchemaPeriod;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductImportDiff;
import com.lombardrisk.ignis.server.product.table.model.Table;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyList;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Strict.class)
public class ProductConfigImportDifferTest {

    @Mock
    private ProductConfigRepository productConfigRepository;

    @InjectMocks
    private ProductConfigImportDiffer productConfigImportDiffer;

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void calculateDiff_NewProductWithSingleVersionedSchemas_ReturnsDiffWithNewSchemasOnly() {
        when(productConfigRepository.findAllPreviousSchemas("P", newHashSet("A", "B")))
                .thenReturn(emptyList());

        ProductImportDiff productImportDiff = productConfigImportDiffer.calculateDiff(
                ProductPopulated.productConfig()
                        .name("P").version("1")
                        .tables(newHashSet(
                                ProductPopulated.table().physicalTableName("A").version(1).build(),
                                ProductPopulated.table().physicalTableName("B").version(2).build()))
                        .build());

        soft.assertThat(productImportDiff.getProductName())
                .isEqualTo("P");
        soft.assertThat(productImportDiff.getProductVersion())
                .isEqualTo("1");
        soft.assertThat(productImportDiff.getExistingSchemas())
                .isEmpty();
        soft.assertThat(productImportDiff.getNewVersionedSchemas())
                .isEmpty();
        soft.assertThat(productImportDiff.getExistingSchemaToNewPeriod())
                .isEmpty();
        soft.assertThat(productImportDiff.getNewSchemas())
                .extracting(Table::getVersionedName)
                .containsExactlyInAnyOrder("A v.1", "B v.2");
    }

    @Test
    public void calculateDiff_NewProductWithMultipleVersions_ReturnsDiffWithNewSchemasAndNewVersionedSchemas() {
        ProductImportDiff productImportDiff = productConfigImportDiffer.calculateDiff(
                ProductPopulated.productConfig()
                        .name("P").version("1")
                        .tables(newHashSet(
                                ProductPopulated.table().physicalTableName("A").version(1).build(),
                                ProductPopulated.table().physicalTableName("A").version(2).build()))
                        .build());

        soft.assertThat(productImportDiff.getExistingSchemas())
                .isEmpty();
        soft.assertThat(productImportDiff.getNewSchemas())
                .extracting(Table::getVersionedName)
                .containsExactly("A v.1");

        soft.assertThat(productImportDiff.getNewVersionedSchemas())
                .extracting(Table::getVersionedName)
                .containsExactly("A v.2");
    }

    @Test
    public void calculateDiff_NewProductWithExistingSchemasOnly_ReturnsDiffWithExistingSchemasOnly() {
        when(productConfigRepository.findAllPreviousSchemas("P", newHashSet("A")))
                .thenReturn(newArrayList(
                        ProductPopulated.table().physicalTableName("A").version(1).build(),
                        ProductPopulated.table().physicalTableName("A").version(2).build(),
                        ProductPopulated.table().physicalTableName("A").version(11).build()));

        ProductImportDiff productImportDiff = productConfigImportDiffer.calculateDiff(
                ProductPopulated.productConfig()
                        .name("P").version("1")
                        .tables(newHashSet(
                                ProductPopulated.table().physicalTableName("A").version(11).build()))
                        .build());

        soft.assertThat(productImportDiff.getNewSchemas())
                .isEmpty();
        soft.assertThat(productImportDiff.getNewVersionedSchemas())
                .isEmpty();
        soft.assertThat(productImportDiff.getExistingSchemas())
                .extracting(Table::getVersionedName)
                .containsExactlyInAnyOrder("A v.1", "A v.2", "A v.11");
    }

    @Test
    public void calculateDiff_NewVersionedProductWithNewSchemasOnly_ReturnsDiffWithNewSchemasAndExistingSchemasOnly() {
        when(productConfigRepository.findAllPreviousSchemas("P", newHashSet("A", "B", "C", "D")))
                .thenReturn(newArrayList(
                        ProductPopulated.table().physicalTableName("C").version(1).build(),
                        ProductPopulated.table().physicalTableName("D").version(2).build()));

        ProductImportDiff productImportDiff = productConfigImportDiffer.calculateDiff(
                ProductPopulated.productConfig()
                        .name("P").version("2")
                        .tables(newHashSet(
                                ProductPopulated.table().physicalTableName("A").version(1).build(),
                                ProductPopulated.table().physicalTableName("B").version(1).build(),
                                ProductPopulated.table().physicalTableName("C").version(1).build(),
                                ProductPopulated.table().physicalTableName("D").version(2).build()))
                        .build());

        soft.assertThat(productImportDiff.getNewVersionedSchemas())
                .isEmpty();
        soft.assertThat(productImportDiff.getExistingSchemas())
                .extracting(Table::getVersionedName)
                .containsExactlyInAnyOrder("C v.1", "D v.2");
        soft.assertThat(productImportDiff.getNewSchemas())
                .extracting(Table::getVersionedName)
                .containsExactlyInAnyOrder("A v.1", "B v.1");
    }

    @Test
    public void calculateDiff_NewVersionedProductWithNewVersionedSchemasOnly_ReturnsDiffWithNewVersionedSchemasAndExistingSchemasOnly() {
        when(productConfigRepository.findAllPreviousSchemas("P", newHashSet("A", "B")))
                .thenReturn(newArrayList(
                        ProductPopulated.table().physicalTableName("A").version(1).build(),
                        ProductPopulated.table().physicalTableName("B").version(1).build()));

        ProductImportDiff productImportDiff = productConfigImportDiffer.calculateDiff(
                ProductPopulated.productConfig()
                        .name("P").version("2")
                        .tables(newHashSet(
                                ProductPopulated.table().physicalTableName("A").version(1).build(),
                                ProductPopulated.table().physicalTableName("A").version(11).build(),
                                ProductPopulated.table().physicalTableName("B").version(1).build(),
                                ProductPopulated.table().physicalTableName("B").version(22).build()))
                        .build());

        soft.assertThat(productImportDiff.getNewSchemas())
                .isEmpty();
        soft.assertThat(productImportDiff.getExistingSchemas())
                .extracting(Table::getVersionedName)
                .containsExactlyInAnyOrder("A v.1", "B v.1");
        soft.assertThat(productImportDiff.getNewVersionedSchemas())
                .extracting(Table::getVersionedName)
                .containsExactlyInAnyOrder("A v.11", "B v.22");
    }

    @Test
    public void calculateDiff_NewVersionedProduct_ReturnsDiffWithAllTypesOfSchemas() {
        when(productConfigRepository.findAllPreviousSchemas("P", newHashSet("A", "B")))
                .thenReturn(newArrayList(
                        ProductPopulated.table().physicalTableName("A").version(1).build()));

        ProductImportDiff productImportDiff = productConfigImportDiffer.calculateDiff(
                ProductPopulated.productConfig()
                        .name("P").version("2")
                        .tables(newHashSet(
                                ProductPopulated.table().physicalTableName("A").version(1).build(),
                                ProductPopulated.table().physicalTableName("A").version(3).build(),
                                ProductPopulated.table().physicalTableName("B").version(1).build(),
                                ProductPopulated.table().physicalTableName("B").version(3).build()))
                        .build());

        soft.assertThat(productImportDiff.getExistingSchemas())
                .extracting(Table::getVersionedName)
                .containsExactly("A v.1");
        soft.assertThat(productImportDiff.getNewSchemas())
                .extracting(Table::getVersionedName)
                .containsExactly("B v.1");
        soft.assertThat(productImportDiff.getNewVersionedSchemas())
                .extracting(Table::getVersionedName)
                .containsExactlyInAnyOrder("A v.3", "B v.3");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void calculateDiff_NewProductWithNewSchemaPeriods_ReturnsDiffWithNewSchemaPeriods() {
        Table existingSchema1 = ProductPopulated.table()
                .physicalTableName("A").version(1)
                .startDate(LocalDate.of(1970, 1, 1))
                .endDate(null)
                .build();
        Table existingSchema2 = ProductPopulated.table()
                .physicalTableName("B").version(1)
                .startDate(LocalDate.of(1990, 1, 1))
                .endDate(LocalDate.of(1999, 1, 1))
                .build();

        when(productConfigRepository.findAllPreviousSchemas("P", newHashSet("A", "B")))
                .thenReturn(newArrayList(existingSchema1, existingSchema2));

        ProductImportDiff productImportDiff = productConfigImportDiffer.calculateDiff(
                ProductPopulated.productConfig()
                        .name("P").version("1")
                        .tables(newHashSet(
                                ProductPopulated.table().physicalTableName("A").version(1)
                                        .startDate(LocalDate.of(2000, 1, 1))
                                        .endDate(LocalDate.of(2010, 10, 10))
                                        .build(),
                                ProductPopulated.table().physicalTableName("B").version(1)
                                        .startDate(LocalDate.of(2100, 1, 1))
                                        .endDate(LocalDate.of(2110, 10, 10))
                                        .build()))
                        .build());

        soft.assertThat(productImportDiff.getExistingSchemaToNewPeriod().entrySet())
                .extracting(Map.Entry::getKey, Map.Entry::getValue)
                .containsExactlyInAnyOrder(
                        tuple(
                                existingSchema1,
                                SchemaPeriod.between(LocalDate.of(2000, 1, 1), LocalDate.of(2010, 10, 10))),
                        tuple(
                                existingSchema2,
                                SchemaPeriod.between(LocalDate.of(2100, 1, 1), LocalDate.of(2110, 10, 10))));
    }
}
