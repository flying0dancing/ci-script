package com.lombardrisk.ignis.server.dataset.rule;

import com.lombardrisk.ignis.api.rule.ValidationOutput;
import com.lombardrisk.ignis.server.dataset.config.DatasetTest;
import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import com.lombardrisk.ignis.server.dataset.fixture.JdbcFixture;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.dataset.rule.detail.ValidationDetailQuery;
import com.lombardrisk.ignis.server.dataset.rule.detail.ValidationDetailRepository;
import com.lombardrisk.ignis.server.dataset.rule.detail.ValidationDetailRow;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import com.lombardrisk.ignis.server.product.table.model.StringField;
import com.lombardrisk.ignis.server.product.table.model.Table;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.lombardrisk.ignis.api.rule.ValidationOutput.VALIDATION_RESULT_TYPE;
import static com.lombardrisk.ignis.pipeline.step.api.DatasetFieldName.ROW_KEY;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;

@RunWith(SpringRunner.class)
@DatasetTest
public class ValidationDetailRepositoryPhoenixIT {

    private final List<JdbcFixture.ColumnDefinition> validationTableColumnHeaders = Arrays.asList(
            new JdbcFixture.ColumnDefinition(ValidationOutput.DATASET_ROW_KEY, "INTEGER"),
            new JdbcFixture.ColumnDefinition(ValidationOutput.DATASET_ID, "INTEGER"),
            new JdbcFixture.ColumnDefinition(ValidationOutput.VALIDATION_RULE_ID, "INTEGER"),
            new JdbcFixture.ColumnDefinition(VALIDATION_RESULT_TYPE, "VARCHAR(255)")
    );

    private final List<JdbcFixture.ColumnDefinition> columnHeaders = Arrays.asList(
            new JdbcFixture.ColumnDefinition(ROW_KEY.name(), "INTEGER"),
            new JdbcFixture.ColumnDefinition("NAME", "VARCHAR(255)")
    );

    @Rule
    public final JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcFixture jdbcFixture;
    private ValidationDetailRepository validationDetailRepository;

    @Before
    public void setUp() {
        jdbcFixture = new JdbcFixture(jdbcTemplate);
        validationDetailRepository = new ValidationDetailRepository(jdbcTemplate);
    }

    @SuppressWarnings("all")
    @Test
    public void findValidationDetails_sortedByNamePageRequest_ReturnsPageOneSortedByName() {
        jdbcFixture.insertData("HOBBITS", columnHeaders, Arrays.asList(
                Arrays.asList(1, "BILBO"),
                Arrays.asList(2, "SAM"),
                Arrays.asList(3, "FRODO"),
                Arrays.asList(4, "ROSIE")
        ));

        jdbcFixture.insertData(ValidationOutput.VALIDATION_RULE_RESULTS, validationTableColumnHeaders, Arrays.asList(
                Arrays.asList(1, 1, 900, "SUCCESS"),
                Arrays.asList(2, 1, 900, "FAIL"),
                Arrays.asList(3, 1, 900, "ERROR"),
                Arrays.asList(4, 1, 900, "SUCCESS")
        ));

        StringField stringField = new StringField();
        stringField.setName("NAME");

        Table hobbits = ProductPopulated.table()
                .physicalTableName("HOBBITS")
                .fields(singleton(stringField))
                .build();

        ValidationDetailQuery query = ValidationDetailQuery.allRules(
                DatasetPopulated.dataset()
                        .id(1L)
                        .name("HOBBITS")
                        .schema(hobbits)
                        .build(),
                PageRequest.of(0, 2, Sort.by(Sort.Order.desc("NAME"))));

        Page<ValidationDetailRow> validationDetails = validationDetailRepository.findValidationDetails(query);

        soft.assertThat(validationDetails.getTotalPages())
                .isEqualTo(2);
        soft.assertThat(validationDetails.getNumberOfElements())
                .isEqualTo(2);
        soft.assertThat(validationDetails.getTotalElements())
                .isEqualTo(4);

        Map<String, Object> rowOne = validationDetails.getContent().get(0).getValues();
        Map<String, Object> rowTwo = validationDetails.getContent().get(1).getValues();
        soft.assertThat(rowOne.keySet())
                .contains("NAME", "RESULT_TYPE");
        soft.assertThat(rowOne.values())
                .contains("SAM", "FAIL");
        soft.assertThat(rowTwo.keySet())
                .contains("NAME", "RESULT_TYPE");
        soft.assertThat(rowTwo.values())
                .contains("ROSIE", "SUCCESS");
    }

    @Test
    public void findValidationDetails_unsorted_executesSuccessfully() {
        jdbcFixture.insertData("HOBBITS", columnHeaders, singletonList(
                Arrays.asList(1, "BILBO")
        ));

        jdbcFixture.insertData(ValidationOutput.VALIDATION_RULE_RESULTS, validationTableColumnHeaders, singletonList(
                Arrays.asList(1, 1, 900, "SUCCESS")
        ));

        StringField stringField = new StringField();
        stringField.setName("NAME");

        Table hobbits = ProductPopulated.table()
                .physicalTableName("HOBBITS")
                .fields(singleton(stringField))
                .build();

        ValidationDetailQuery query = ValidationDetailQuery.allRules(
                DatasetPopulated.dataset()
                        .id(1L)
                        .name("HOBBITS")
                        .schema(hobbits)
                        .build(),
                PageRequest.of(0, 2, Sort.unsorted()));

        Page<ValidationDetailRow> validationDetails = validationDetailRepository.findValidationDetails(query);

        soft.assertThat(validationDetails.getTotalPages())
                .isEqualTo(2);
        soft.assertThat(validationDetails.getNumberOfElements())
                .isEqualTo(1);
        soft.assertThat(validationDetails.getTotalElements())
                .isEqualTo(4);
    }

    @SuppressWarnings("all")
    @Test
    public void findValidationDetails_sortedByResultTypePageRequest_ReturnsPageOneSortedByResultType() {
        jdbcFixture.insertData("HOBBITS", columnHeaders, Arrays.asList(
                Arrays.asList(1, "BILBO"),
                Arrays.asList(2, "SAM"),
                Arrays.asList(3, "FRODO"),
                Arrays.asList(4, "ROSIE")
        ));

        jdbcFixture.insertData(ValidationOutput.VALIDATION_RULE_RESULTS, validationTableColumnHeaders, Arrays.asList(
                Arrays.asList(1, 901, 10012, "ERROR"),
                Arrays.asList(2, 901, 10012, "FAIL"),
                Arrays.asList(3, 901, 10012, "ERROR"),
                Arrays.asList(4, 901, 10012, "SUCCESS")
        ));

        StringField stringField = new StringField();
        stringField.setName("NAME");

        Table hobbits = ProductPopulated.table()
                .physicalTableName("HOBBITS")
                .fields(singleton(stringField))
                .build();

        ValidationDetailQuery query = ValidationDetailQuery.allRules(
                DatasetPopulated.dataset()
                        .id(901L)
                        .name("HOBBITS")
                        .schema(hobbits)
                        .build(),
                PageRequest.of(0, 2, Sort.by(Sort.Order.desc(VALIDATION_RESULT_TYPE))));

        Page<ValidationDetailRow> validationDetails = validationDetailRepository.findValidationDetails(query);

        soft.assertThat(validationDetails.getTotalPages())
                .isEqualTo(2);
        soft.assertThat(validationDetails.getNumberOfElements())
                .isEqualTo(2);
        soft.assertThat(validationDetails.getTotalElements())
                .isEqualTo(4);

        Map<String, Object> rowOne = validationDetails.getContent().get(0).getValues();
        Map<String, Object> rowTwo = validationDetails.getContent().get(1).getValues();
        soft.assertThat(rowOne.keySet())
                .contains("NAME", "RESULT_TYPE");
        soft.assertThat(rowOne.values())
                .containsExactly("ROSIE", "SUCCESS");
        soft.assertThat(rowTwo.keySet())
                .contains("NAME", "RESULT_TYPE");
        soft.assertThat(rowTwo.values())
                .contains("SAM", "FAIL");
    }

    @SuppressWarnings("all")
    @Test
    public void findValidationDetails_filterByRuleId_ReturnsOnlyRowsForRuleId() {
        jdbcFixture.insertData("HOBBITS", columnHeaders, Arrays.asList(
                Arrays.asList(1, "BILBO"),
                Arrays.asList(2, "SAM"),
                Arrays.asList(3, "FRODO"),
                Arrays.asList(4, "ROSIE"),
                Arrays.asList(5, "PEREGRIN")
        ));

        jdbcFixture.insertData(ValidationOutput.VALIDATION_RULE_RESULTS, validationTableColumnHeaders, Arrays.asList(
                Arrays.asList(1, 901, 55, "ERROR"),
                Arrays.asList(2, 901, 44, "FAIL"),
                Arrays.asList(3, 901, 44, "ERROR"),
                Arrays.asList(4, 901, 44, "SUCCESS"),
                Arrays.asList(5, 901, 55, "SUCCESS")
        ));

        StringField stringField = new StringField();
        stringField.setName("NAME");

        Table hobbits = ProductPopulated.table()
                .physicalTableName("HOBBITS")
                .fields(singleton(stringField))
                .build();

        Dataset hobbitsDataset = DatasetPopulated.dataset()
                .id(901L)
                .name("HOBBITS")
                .schema(hobbits)
                .build();

        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by(Sort.Order.desc("NAME")));
        ValidationRule validationRule = ProductPopulated.validationRule().id(55L).build();

        ValidationDetailQuery query = ValidationDetailQuery.byRule(hobbitsDataset, validationRule, pageRequest);
        Page<ValidationDetailRow> validationDetails = validationDetailRepository.findValidationDetails(query);

        soft.assertThat(validationDetails.getTotalPages())
                .isEqualTo(2);
        soft.assertThat(validationDetails.getNumberOfElements())
                .isEqualTo(2);
        soft.assertThat(validationDetails.getTotalElements())
                .isEqualTo(4);

        Map<String, Object> rowOne = validationDetails.getContent().get(0).getValues();
        Map<String, Object> rowTwo = validationDetails.getContent().get(1).getValues();
        soft.assertThat(rowOne.values())
                .containsExactly("PEREGRIN", "SUCCESS");
        soft.assertThat(rowTwo.values())
                .contains("BILBO", "ERROR");
    }
}
