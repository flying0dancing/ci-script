package com.lombardrisk.ignis.server.product.table.model;

import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import com.lombardrisk.ignis.common.fixtures.BeanValidationAssertions;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.IOException;

import static com.google.common.collect.Lists.newArrayList;
import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static java.util.Collections.singleton;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(Enclosed.class)
public class TableTest {

    public static class VersionEquality {

        @Test
        public void isSameVersionAs_TableWithSameNameAndVersion_ReturnsTrue() {
            assertThat(
                    ProductPopulated.table()
                            .physicalTableName("A").version(1)
                            .build()
                            .isSameVersionAs(
                                    ProductPopulated.table()
                                            .physicalTableName("A").version(1)
                                            .build())
            ).isTrue();
        }

        @Test
        public void isSameVersionAs_TableWithSameUppercaseNameAndVersion_ReturnsTrue() {
            assertThat(
                    ProductPopulated.table()
                            .physicalTableName("A").version(1)
                            .build()
                            .isSameVersionAs(
                                    ProductPopulated.table()
                                            .physicalTableName("a").version(1)
                                            .build())
            ).isTrue();
        }

        @Test
        public void isSameVersionAs_TableWithDifferentNameAndVersion_ReturnsFalse() {
            assertThat(
                    ProductPopulated.table()
                            .physicalTableName("A").version(1)
                            .build()
                            .isSameVersionAs(
                                    ProductPopulated.table()
                                            .physicalTableName("a").version(2)
                                            .build())
            ).isFalse();
        }

        @Test
        public void isSameVersionAs_NullOtherTable_ThrowsException() {
            assertThatThrownBy(() ->
                    ProductPopulated.table()
                            .physicalTableName("A").version(1)
                            .build()
                            .isSameVersionAs(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("other table cannot be null");
        }
    }

    public static class DeserializationTest {

        @Test
        public void deserializeFields_PreservesInsertionOrder() throws IOException {
            String json = "{\n"
                    + "    \"physicalTableName\": \"TRADES_DATA_SCHEDULES\",\n"
                    + "    \"fields\": [\n"
                    + "        {\n"
                    + "            \"name\": \"TRADE_DATE\",\n"
                    + "            \"type\": \"int\",\n"
                    + "            \"nullable\": false\n"
                    + "        },\n"
                    + "        {\n"
                    + "            \"name\": \"SETTLEMENT_DATE\",\n"
                    + "            \"type\": \"int\",\n"
                    + "            \"nullable\": false\n"
                    + "        },\n"
                    + "        {\n"
                    + "            \"name\": \"STOCK\",\n"
                    + "            \"type\": \"string\",\n"
                    + "            \"nullable\": false\n"
                    + "        },\n"
                    + "        {\n"
                    + "            \"name\": \"TRADE_TYPE\",\n"
                    + "            \"type\": \"string\",\n"
                    + "            \"nullable\": false\n"
                    + "        },\n"
                    + "        {\n"
                    + "            \"name\": \"RATE\",\n"
                    + "            \"type\": \"decimal\",\n"
                    + "            \"nullable\": false,\n"
                    + "            \"precision\": 7,\n"
                    + "            \"scale\": 2\n"
                    + "        },\n"
                    + "        {\n"
                    + "            \"name\": \"AMOUNT\",\n"
                    + "            \"type\": \"decimal\",\n"
                    + "            \"nullable\": false,\n"
                    + "            \"precision\": 7,\n"
                    + "            \"scale\": 2\n"
                    + "        },\n"
                    + "        {\n"
                    + "            \"name\": \"CALCULATED_VALUE\",\n"
                    + "            \"type\": \"decimal\",\n"
                    + "            \"nullable\": false,\n"
                    + "            \"precision\": 7,\n"
                    + "            \"scale\": 2\n"
                    + "        }\n"
                    + "\t]\n"
                    + "}";

            Table table = MAPPER.readValue(json, Table.class);

            assertThat(newArrayList(table.getFields()))
                    .extracting(Field::getName)
                    .containsExactly(
                            "TRADE_DATE",
                            "SETTLEMENT_DATE",
                            "STOCK",
                            "TRADE_TYPE",
                            "RATE",
                            "AMOUNT",
                            "CALCULATED_VALUE");
        }
    }

    public static class BeanValidationTest {

        private static BeanValidationAssertions<Table> assertThat(final Table table) {
            return BeanValidationAssertions.assertThat(table);
        }

        @Test
        public void validate_NameWithCorrectFormat_ReturnsNoConstraintViolations() {
            assertThat(
                    ProductPopulated.table()
                            .physicalTableName("V_A_L_1_D_name")
                            .build())
                    .hasNoViolations();
        }

        @Test
        public void validate_NullName_ReturnsConstraintViolation() {
            assertThat(
                    ProductPopulated.table()
                            .physicalTableName(null)
                            .build())
                    .containsViolation("physicalTableName", "must not be null");
        }

        @Test
        public void validate_NullVersion_ReturnsConstraintViolation() {
            assertThat(
                    ProductPopulated.table().version(null)
                            .build())
                    .containsViolation("version", "must not be null");
        }

        @Test
        public void validate_NameWithOutOfBoundsSize_ReturnsConstraintViolation() {
            assertThat(
                    ProductPopulated.table()
                            .physicalTableName("")
                            .build())
                    .containsViolation("physicalTableName", "size must be between 1 and 31");

            assertThat(
                    ProductPopulated.table()
                            .physicalTableName(repeat("t", 32))
                            .build())
                    .containsViolation("physicalTableName", "size must be between 1 and 31");
        }

        @Test
        public void validate_NameWithNonMatchingPattern_ReturnsConstraintViolation() {
            assertThat(
                    ProductPopulated.table()
                            .physicalTableName("Spaces are hard")
                            .build())
                    .containsViolation(
                            "physicalTableName",
                            "PhysicalTableName has to start with at least 1 word character, followed by 0 or more word characters or '_'");

            assertThat(
                    ProductPopulated.table()
                            .physicalTableName("ALL_D0LAR$$")
                            .build())
                    .containsViolation(
                            "physicalTableName",
                            "PhysicalTableName has to start with at least 1 word character, followed by 0 or more word characters or '_'");

            assertThat(
                    ProductPopulated.table()
                            .physicalTableName("P|PED")
                            .build())
                    .containsViolation(
                            "physicalTableName",
                            "PhysicalTableName has to start with at least 1 word character, followed by 0 or more word characters or '_'");

            assertThat(
                    ProductPopulated.table()
                            .physicalTableName("3CP0")
                            .build())
                    .containsViolation(
                            "physicalTableName",
                            "PhysicalTableName has to start with at least 1 word character, followed by 0 or more word characters or '_'");
        }

        @Test
        public void validate_FieldsWithViolation_ReturnsCascadedConstraintViolation() {
            assertThat(
                    ProductPopulated.table()
                            .fields(singleton(new DateField()))
                            .build())
                    .containsViolation("fields[].name", "must not be null");
        }

        @Test
        public void validate_RulesWithViolations_ReturnsCascadedConstraintViolation() {
            ValidationRule ruleIdBlank = ProductPopulated.validationRule().ruleId(null).build();

            assertThat(
                    ProductPopulated.table()
                            .validationRules(singleton(ruleIdBlank))
                            .build())
                    .containsViolation("validationRules[].ruleId", "must not be blank");
        }
    }
}
