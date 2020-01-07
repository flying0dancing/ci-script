package com.lombardrisk.ignis.server.dataset.rule.detail;

import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import com.lombardrisk.ignis.server.product.table.model.IntField;
import com.lombardrisk.ignis.server.product.table.model.StringField;
import io.vavr.control.Validation;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

public class ValidationDetailQueryTest {

    @Test
    public void all_sortFieldsNotDefinedInDataset_ReturnsValidationFailure() {
        StringField nameField = new StringField();
        nameField.setName("name");

        Dataset dataset = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .fields(singleton(nameField))
                        .build())
                .build();

        PageRequest invalid = PageRequest.of(0, 100, Sort.by(Sort.Order.desc("invalidParam")));

        Validation<List<String>, ValidationDetailQuery> query = ValidationDetailQuery.allRules(dataset, invalid)
                .validate();

        assertThat(query.getError())
                .contains("invalidParam");
    }

    @Test
    public void getSql_sortFieldsDefinedInDataset_ReturnsSortedSql() {
        StringField nameField = new StringField();
        nameField.setName("name");

        IntField ageField = new IntField();
        ageField.setName("age");

        Dataset dataset = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .fields(ImmutableSet.of(nameField, ageField))
                        .build())
                .build();

        PageRequest sortByNameAndAge = PageRequest.of(0, 100, Sort.by(Sort.Order.desc("name"), Sort.Order.asc("age")));

        ValidationDetailQuery query = ValidationDetailQuery.allRules(dataset, sortByNameAndAge);

        assertThat(query.getSqlQuery())
                .containsSequence(" ORDER BY \"name\" DESC,\"age\" ASC ")
                .isEqualTo("SELECT \"name\",\"age\",\"RESULT_TYPE\" "
                        + "FROM CQRS dataset "
                        + "JOIN VALIDATION_RULE_RESULTS v ON dataset.ROW_KEY = v.DATASET_ROW_KEY WHERE v.DATASET_ID = null "
                        + "ORDER BY \"name\" DESC,\"age\" ASC LIMIT 100 OFFSET 0");
    }

    @Test
    public void getSql_paginatedRequestNoSorting_ReturnsLimitAndOffsetSql() {
        StringField nameField = new StringField();
        nameField.setName("name");

        Dataset dataset = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .fields(singleton(nameField))
                        .build())
                .build();

        PageRequest sortByNameAndAge = PageRequest.of(0, 100);

        ValidationDetailQuery query = ValidationDetailQuery.allRules(dataset, sortByNameAndAge);

        assertThat(query.getSqlQuery())
                .containsSequence(" LIMIT 100 OFFSET 0")
                .isEqualTo("SELECT \"name\",\"RESULT_TYPE\" "
                        + "FROM CQRS dataset "
                        + "JOIN VALIDATION_RULE_RESULTS v ON dataset.ROW_KEY = v.DATASET_ROW_KEY WHERE v.DATASET_ID = null "
                        + "LIMIT 100 OFFSET 0");
    }

    @Test
    public void getSql_datasetTable_ReturnsSqlSelectingDatasetColumns() {
        Dataset dataset = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .fields(ImmutableSet.of(
                                aField("name"), aField("age"), aField("favouriteSimpsonsCharacter")))
                        .build())
                .build();

        PageRequest sortByNameAndAge = PageRequest.of(0, 100);

        ValidationDetailQuery query = ValidationDetailQuery.allRules(dataset, sortByNameAndAge);

        assertThat(query.getSqlQuery())
                .containsSequence("SELECT \"name\",\"age\",\"favouriteSimpsonsCharacter\"")
                .isEqualTo("SELECT \"name\",\"age\",\"favouriteSimpsonsCharacter\",\"RESULT_TYPE\" "
                        + "FROM CQRS dataset JOIN VALIDATION_RULE_RESULTS v "
                        + "ON dataset.ROW_KEY = v.DATASET_ROW_KEY WHERE v.DATASET_ID = null LIMIT 100 OFFSET 0");
    }

    @Test
    public void getSql_datasetTable_ReturnsSqlSelectingValidationResultType() {
        Dataset dataset = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .fields(singleton(
                                aField("name")))
                        .build())
                .build();

        ValidationDetailQuery query = ValidationDetailQuery.allRules(dataset, PageRequest.of(0, 100));

        assertThat(query.getSqlQuery())
                .containsSequence("SELECT \"name\",\"RESULT_TYPE\" ")
                .isEqualTo("SELECT \"name\",\"RESULT_TYPE\" "
                        + "FROM CQRS dataset JOIN VALIDATION_RULE_RESULTS v "
                        + "ON dataset.ROW_KEY = v.DATASET_ROW_KEY WHERE v.DATASET_ID = null LIMIT 100 OFFSET 0");
    }

    @Test
    public void getSql_dataset_ReturnsSqlFilteredByDatasetId() {
        Dataset dataset = DatasetPopulated.dataset()
                .id(100L)
                .schema(ProductPopulated.table()
                        .fields(singleton(
                                aField("name")))
                        .build())
                .build();

        ValidationDetailQuery query = ValidationDetailQuery.allRules(dataset, PageRequest.of(0, 100));

        assertThat(query.getSqlQuery())
                .containsSequence(" WHERE v.DATASET_ID = 100 ")
                .isEqualTo("SELECT \"name\",\"RESULT_TYPE\" "
                        + "FROM CQRS dataset JOIN VALIDATION_RULE_RESULTS v "
                        + "ON dataset.ROW_KEY = v.DATASET_ROW_KEY WHERE v.DATASET_ID = 100 LIMIT 100 OFFSET 0");
    }

    @Test
    public void getSql_validationRule_ReturnsSqlFilteredByValidationRule() {
        Dataset dataset = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .fields(singleton(
                                aField("name")))
                        .build())
                .build();

        ValidationRule validationRule = ProductPopulated.validationRule()
                .id(1992L)
                .build();

        ValidationDetailQuery query = ValidationDetailQuery.byRule(dataset, validationRule, PageRequest.of(0, 100));

        assertThat(query.getSqlQuery())
                .containsSequence(" AND v.VALIDATION_RULE_ID = 1992 ")
                .isEqualTo("SELECT \"name\",\"RESULT_TYPE\" "
                        + "FROM CQRS dataset JOIN VALIDATION_RULE_RESULTS v "
                        + "ON dataset.ROW_KEY = v.DATASET_ROW_KEY WHERE v.DATASET_ID = null "
                        + "AND v.VALIDATION_RULE_ID = 1992 LIMIT 100 OFFSET 0");
    }

    @Test
    public void getRecordsCount_allRules_ReturnsRecordsMultipliedByRules() {
        StringField nameField = new StringField();
        nameField.setName("name");

        IntField ageField = new IntField();
        ageField.setName("age");

        Dataset dataset = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .fields(ImmutableSet.of(nameField, ageField))
                        .validationRules(ImmutableSet.of(
                                ProductPopulated.validationRule().build(),
                                ProductPopulated.validationRule().build(),
                                ProductPopulated.validationRule().build()))
                        .build())
                .recordsCount(100L)
                .build();

        PageRequest sortByNameAndAge = PageRequest.of(0, 100, Sort.by(Sort.Order.desc("name"), Sort.Order.asc("age")));

        ValidationDetailQuery query = ValidationDetailQuery.allRules(dataset, sortByNameAndAge);

        assertThat(query.getRecordsCount()).isEqualTo(300);
    }

    @Test
    public void getRecordsCount_oneRule_ReturnsRecords() {
        StringField nameField = new StringField();
        nameField.setName("name");

        IntField ageField = new IntField();
        ageField.setName("age");

        Dataset dataset = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .fields(ImmutableSet.of(nameField, ageField))
                        .build())
                .recordsCount(100L)
                .build();

        PageRequest sortByNameAndAge = PageRequest.of(0, 100, Sort.by(Sort.Order.desc("name"), Sort.Order.asc("age")));

        ValidationDetailQuery query =
                ValidationDetailQuery.byRule(dataset, ProductPopulated.validationRule().build(), sortByNameAndAge);

        assertThat(query.getRecordsCount()).isEqualTo(100);
    }

    private StringField aField(final String name) {
        StringField aField = new StringField();
        aField.setName(name);
        return aField;
    }
}
