package com.lombardrisk.ignis.design.server.productconfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lombardrisk.ignis.client.external.pipeline.export.PipelineExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineMapStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.SchemaReference;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.client.external.productconfig.export.ValidationRuleExport;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport;
import com.lombardrisk.ignis.client.external.productconfig.export.ProductConfigExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaPeriod;
import com.lombardrisk.ignis.design.server.productconfig.export.ProductConfigZipObjectMapper;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.util.Date;

import static com.lombardrisk.ignis.common.fixtures.PopulatedDates.toDate;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@RunWith(Enclosed.class)
public class ProductConfigZipObjectMapperTest {

    public static class ProductConfigTest {

        @Rule
        public JUnitSoftAssertions soft = new JUnitSoftAssertions();
        private ProductConfigZipObjectMapper productConfigZipObjectMapper = new ProductConfigZipObjectMapper();
        private ObjectMapper objectMapper = new ObjectMapper();

        @Test
        public void writeProductConfigView_ProductConfigWithTable_WritesProductConfigJson() throws Exception {
            ProductConfigExport productConfigView = ProductConfigExport.builder()
                    .id(123L)
                    .name("new product config view")
                    .version("v1.0")
                    .tables(singletonList(SchemaExport.builder().build()))
                    .build();

            byte[] jsonByteArray = productConfigZipObjectMapper.writeProductConfigView(productConfigView);
            JsonNode jsonNode = objectMapper.readTree(jsonByteArray);

            soft.assertThat(jsonNode.fieldNames()).containsOnly("name", "version");
            soft.assertThat(jsonNode.get("name").textValue()).isEqualTo("new product config view");
            soft.assertThat(jsonNode.get("version").textValue()).isEqualTo("v1.0");
        }
    }

    public static class TableTest {

        @Rule
        public JUnitSoftAssertions soft = new JUnitSoftAssertions();

        private ProductConfigZipObjectMapper productConfigZipObjectMapper = new ProductConfigZipObjectMapper();
        private ObjectMapper objectMapper = new ObjectMapper();

        @Test
        public void writeTableView_TableWithoutFieldsWithoutRules_WritesTableJson() throws Exception {
            Date createdTime = new Date();
            SchemaExport schemaView = SchemaExport.builder()
                    .id(432L)
                    .physicalTableName("new table name")
                    .displayName("new table display name")
                    .startDate(toDate("2000-1-1"))
                    .endDate(toDate("2001-1-1"))
                    .createdBy("me")
                    .createdTime(createdTime)
                    .version(4)
                    .period(SchemaPeriod.between(
                            LocalDate.of(2000, 1, 1), LocalDate.of(2001, 1, 1)))
                    .build();

            byte[] jsonByteArray = productConfigZipObjectMapper.writeTableExport(schemaView);
            JsonNode jsonNode = objectMapper.readTree(jsonByteArray);

            soft.assertThat(jsonNode.fieldNames())
                    .containsOnly(
                            "physicalTableName",
                            "displayName",
                            "startDate",
                            "endDate",
                            "createdBy",
                            "fields",
                            "version",
                            "validationRules",
                            "period");

            soft.assertThat(jsonNode.get("physicalTableName").textValue()).isEqualTo("new table name");
            soft.assertThat(jsonNode.get("displayName").textValue()).isEqualTo("new table display name");
            soft.assertThat(jsonNode.get("createdBy").textValue()).isEqualTo("me");
            soft.assertThat(new Date(jsonNode.get("startDate").longValue())).isEqualTo(toDate("2000-01-01"));
            soft.assertThat(new Date(jsonNode.get("endDate").longValue())).isEqualTo(toDate("2001-01-01"));
            soft.assertThat(jsonNode.get("period").get("startDate").asText()).isEqualTo("2000-01-01");
            soft.assertThat(jsonNode.get("period").get("endDate").asText()).isEqualTo("2001-01-01");
            soft.assertThat(jsonNode.get("version").intValue()).isEqualTo(4);
            soft.assertThat(jsonNode.get("fields").isNull()).isTrue();
            soft.assertThat(jsonNode.get("validationRules").isNull()).isTrue();
        }

        @Test
        public void writeTableView_TableWithFields_WritesTableJson() throws Exception {
            SchemaExport schemaView = SchemaExport.builder()
                    .fields(asList(
                            FieldExport.StringFieldExport.builder().build(),
                            FieldExport.LongFieldExport.builder().build()))
                    .build();

            byte[] jsonByteArray = productConfigZipObjectMapper.writeTableExport(schemaView);
            JsonNode jsonNode = objectMapper.readTree(jsonByteArray);

            soft.assertThat(jsonNode.get("fields").isArray()).isTrue();
            soft.assertThat(jsonNode.get("fields")).hasSize(2);
        }

        @Test
        public void writeTableView_TableWithRules_WritesTableJson() throws Exception {
            SchemaExport schemaView = SchemaExport.builder()
                    .validationRules(singletonList(ValidationRuleExport.builder()
                            .ruleId("rule id")
                            .name("rule name")
                            .build()))
                    .build();

            byte[] jsonByteArray = productConfigZipObjectMapper.writeTableExport(schemaView);
            JsonNode jsonNode = objectMapper.readTree(jsonByteArray);

            soft.assertThat(jsonNode.get("validationRules").isArray()).isTrue();
            soft.assertThat(jsonNode.get("validationRules")).hasSize(1);
        }
    }

    public static class FieldTest {

        @Rule
        public JUnitSoftAssertions soft = new JUnitSoftAssertions();

        private ProductConfigZipObjectMapper productConfigZipObjectMapper = new ProductConfigZipObjectMapper();
        private ObjectMapper objectMapper = new ObjectMapper();

        @Test
        public void writeTableView_StringField_WritesFieldJson() throws Exception {
            FieldExport fieldView = FieldExport.StringFieldExport.builder()
                    .id(234L)
                    .maxLength(10)
                    .minLength(5)
                    .name("my string field")
                    .nullable(true)
                    .regularExpression("my regex")
                    .build();

            SchemaExport schemaView = SchemaExport.builder().fields(singletonList(fieldView)).build();

            byte[] jsonByteArray = productConfigZipObjectMapper.writeTableExport(schemaView);
            JsonNode jsonNode = objectMapper.readTree(jsonByteArray).get("fields").get(0);

            soft.assertThat(jsonNode.fieldNames())
                    .containsOnly("maxLength", "minLength", "name", "nullable", "regularExpression", "type");
            soft.assertThat(jsonNode.get("maxLength").intValue()).isEqualTo(10);
            soft.assertThat(jsonNode.get("minLength").intValue()).isEqualTo(5);
            soft.assertThat(jsonNode.get("name").textValue()).isEqualTo("my string field");
            soft.assertThat(jsonNode.get("nullable").booleanValue()).isTrue();
            soft.assertThat(jsonNode.get("regularExpression").textValue()).isEqualTo("my regex");
            soft.assertThat(jsonNode.get("type").textValue()).isEqualTo("string");
        }

        @Test
        public void writeTableView_DateField_WritesFieldJson() throws Exception {
            FieldExport fieldView = FieldExport.DateFieldExport.builder()
                    .id(234L)
                    .name("my date field")
                    .nullable(true)
                    .format("dd/MM/yyyy")
                    .build();

            SchemaExport schemaView = SchemaExport.builder().fields(singletonList(fieldView)).build();

            byte[] jsonByteArray = productConfigZipObjectMapper.writeTableExport(schemaView);
            JsonNode jsonNode = objectMapper.readTree(jsonByteArray).get("fields").get(0);

            soft.assertThat(jsonNode.fieldNames())
                    .containsOnly("name", "nullable", "format", "type");
            soft.assertThat(jsonNode.get("name").textValue()).isEqualTo("my date field");
            soft.assertThat(jsonNode.get("nullable").booleanValue()).isTrue();
            soft.assertThat(jsonNode.get("format").textValue()).isEqualTo("dd/MM/yyyy");
            soft.assertThat(jsonNode.get("type").textValue()).isEqualTo("date");
        }

        @Test
        public void writeTableView_TimestampField_WritesFieldJson() throws Exception {
            FieldExport fieldView = FieldExport.TimestampFieldExport.builder()
                    .id(234L)
                    .name("my timestamp field")
                    .nullable(true)
                    .format("dd/MM/yyyy")
                    .build();

            SchemaExport schemaView = SchemaExport.builder().fields(singletonList(fieldView)).build();

            byte[] jsonByteArray = productConfigZipObjectMapper.writeTableExport(schemaView);
            JsonNode jsonNode = objectMapper.readTree(jsonByteArray).get("fields").get(0);

            soft.assertThat(jsonNode.fieldNames())
                    .containsOnly("name", "nullable", "format", "type");
            soft.assertThat(jsonNode.get("name").textValue()).isEqualTo("my timestamp field");
            soft.assertThat(jsonNode.get("nullable").booleanValue()).isTrue();
            soft.assertThat(jsonNode.get("format").textValue()).isEqualTo("dd/MM/yyyy");
            soft.assertThat(jsonNode.get("type").textValue()).isEqualTo("timestamp");
        }

        @Test
        public void writeTableView_DecimalField_WritesFieldJson() throws Exception {
            FieldExport fieldView = FieldExport.DecimalFieldExport.builder()
                    .id(234L)
                    .name("my decimal field")
                    .nullable(true)
                    .precision(10)
                    .scale(5)
                    .build();

            SchemaExport schemaView = SchemaExport.builder().fields(singletonList(fieldView)).build();

            byte[] jsonByteArray = productConfigZipObjectMapper.writeTableExport(schemaView);
            JsonNode jsonNode = objectMapper.readTree(jsonByteArray).get("fields").get(0);

            soft.assertThat(jsonNode.fieldNames())
                    .containsOnly("name", "nullable", "precision", "scale", "type");
            soft.assertThat(jsonNode.get("name").textValue()).isEqualTo("my decimal field");
            soft.assertThat(jsonNode.get("nullable").booleanValue()).isTrue();
            soft.assertThat(jsonNode.get("precision").intValue()).isEqualTo(10);
            soft.assertThat(jsonNode.get("scale").intValue()).isEqualTo(5);
            soft.assertThat(jsonNode.get("type").textValue()).isEqualTo("decimal");
        }

        @Test
        public void writeTableView_BooleanField_WritesFieldJson() throws Exception {
            FieldExport fieldView = FieldExport.BooleanFieldExport.builder()
                    .id(234L)
                    .name("my boolean field")
                    .nullable(true)
                    .build();

            SchemaExport schemaView = SchemaExport.builder().fields(singletonList(fieldView)).build();

            byte[] jsonByteArray = productConfigZipObjectMapper.writeTableExport(schemaView);
            JsonNode jsonNode = objectMapper.readTree(jsonByteArray).get("fields").get(0);

            soft.assertThat(jsonNode.fieldNames())
                    .containsOnly("name", "nullable", "type");
            soft.assertThat(jsonNode.get("name").textValue()).isEqualTo("my boolean field");
            soft.assertThat(jsonNode.get("nullable").booleanValue()).isTrue();
            soft.assertThat(jsonNode.get("type").textValue()).isEqualTo("boolean");
        }

        @Test
        public void writeTableView_DoubleField_WritesFieldJson() throws Exception {
            FieldExport fieldView = FieldExport.DoubleFieldExport.builder()
                    .id(234L)
                    .name("my double field")
                    .nullable(true)
                    .build();

            SchemaExport schemaView = SchemaExport.builder().fields(singletonList(fieldView)).build();

            byte[] jsonByteArray = productConfigZipObjectMapper.writeTableExport(schemaView);
            JsonNode jsonNode = objectMapper.readTree(jsonByteArray).get("fields").get(0);

            soft.assertThat(jsonNode.fieldNames())
                    .containsOnly("name", "nullable", "type");
            soft.assertThat(jsonNode.get("name").textValue()).isEqualTo("my double field");
            soft.assertThat(jsonNode.get("nullable").booleanValue()).isTrue();
            soft.assertThat(jsonNode.get("type").textValue()).isEqualTo("double");
        }

        @Test
        public void writeTableView_FloatField_WritesFieldJson() throws Exception {
            FieldExport fieldView = FieldExport.FloatFieldExport.builder()
                    .id(234L)
                    .name("my float field")
                    .nullable(true)
                    .build();

            SchemaExport schemaView = SchemaExport.builder().fields(singletonList(fieldView)).build();

            byte[] jsonByteArray = productConfigZipObjectMapper.writeTableExport(schemaView);
            JsonNode jsonNode = objectMapper.readTree(jsonByteArray).get("fields").get(0);

            soft.assertThat(jsonNode.fieldNames())
                    .containsOnly("name", "nullable", "type");
            soft.assertThat(jsonNode.get("name").textValue()).isEqualTo("my float field");
            soft.assertThat(jsonNode.get("nullable").booleanValue()).isTrue();
            soft.assertThat(jsonNode.get("type").textValue()).isEqualTo("float");
        }

        @Test
        public void writeTableView_IntegerField_WritesFieldJson() throws Exception {
            FieldExport fieldView = FieldExport.IntegerFieldExport.builder()
                    .id(234L)
                    .name("my integer field")
                    .nullable(true)
                    .build();

            SchemaExport schemaView = SchemaExport.builder().fields(singletonList(fieldView)).build();

            byte[] jsonByteArray = productConfigZipObjectMapper.writeTableExport(schemaView);
            JsonNode jsonNode = objectMapper.readTree(jsonByteArray).get("fields").get(0);

            soft.assertThat(jsonNode.fieldNames())
                    .containsOnly("name", "nullable", "type");
            soft.assertThat(jsonNode.get("name").textValue()).isEqualTo("my integer field");
            soft.assertThat(jsonNode.get("nullable").booleanValue()).isTrue();
            soft.assertThat(jsonNode.get("type").textValue()).isEqualTo("int");
        }

        @Test
        public void writeTableView_LongField_WritesFieldJson() throws Exception {
            FieldExport fieldView = FieldExport.LongFieldExport.builder()
                    .id(234L)
                    .name("my long field")
                    .nullable(true)
                    .build();

            SchemaExport schemaView = SchemaExport.builder().fields(singletonList(fieldView)).build();

            byte[] jsonByteArray = productConfigZipObjectMapper.writeTableExport(schemaView);
            JsonNode jsonNode = objectMapper.readTree(jsonByteArray).get("fields").get(0);

            soft.assertThat(jsonNode.fieldNames())
                    .containsOnly("name", "nullable", "type");
            soft.assertThat(jsonNode.get("name").textValue()).isEqualTo("my long field");
            soft.assertThat(jsonNode.get("nullable").booleanValue()).isTrue();
            soft.assertThat(jsonNode.get("type").textValue()).isEqualTo("long");
        }
    }

    public static class ValidationRuleTest {

        @Rule
        public JUnitSoftAssertions soft = new JUnitSoftAssertions();

        private ProductConfigZipObjectMapper productConfigZipObjectMapper = new ProductConfigZipObjectMapper();
        private ObjectMapper objectMapper = new ObjectMapper();

        @Test
        public void writeTableView_ValidationRule_WritesValidationRuleJson()
                throws Exception {

            ValidationRuleExport ruleView = ValidationRuleExport.builder()
                    .id(1984L)
                    .ruleId("rule id")
                    .validationRuleType(ValidationRuleExport.Type.QUALITY)
                    .validationRuleSeverity(ValidationRuleExport.Severity.CRITICAL)
                    .version(999)
                    .startDate(LocalDate.of(1988, 1, 1))
                    .endDate(LocalDate.of(1989, 1, 1))
                    .name("rule name")
                    .description("rule description")
                    .expression("2 + 2 = 5")
                    .build();

            SchemaExport schemaView = SchemaExport.builder().validationRules(singletonList(ruleView)).build();

            byte[] jsonByteArray = productConfigZipObjectMapper.writeTableExport(schemaView);
            JsonNode jsonNode = objectMapper.readTree(jsonByteArray)
                    .get("validationRules")
                    .get(0);

            soft.assertThat(jsonNode.fieldNames()).containsOnly(
                    "ruleId",
                    "validationRuleType",
                    "validationRuleSeverity",
                    "version",
                    "startDate",
                    "endDate",
                    "name",
                    "description",
                    "expression",
                    "contextFields");

            soft.assertThat(jsonNode.get("ruleId").textValue()).isEqualTo("rule id");
            soft.assertThat(jsonNode.get("validationRuleType").textValue()).isEqualTo("QUALITY");
            soft.assertThat(jsonNode.get("validationRuleSeverity").textValue()).isEqualTo("CRITICAL");
            soft.assertThat(jsonNode.get("version").intValue()).isEqualTo(999);
            soft.assertThat(jsonNode.get("startDate").textValue()).isEqualTo("1988-01-01");
            soft.assertThat(jsonNode.get("endDate").textValue()).isEqualTo("1989-01-01");
            soft.assertThat(jsonNode.get("name").textValue()).isEqualTo("rule name");
            soft.assertThat(jsonNode.get("description").textValue()).isEqualTo("rule description");
            soft.assertThat(jsonNode.get("expression").textValue()).isEqualTo("2 + 2 = 5");
        }
    }

    public static class PipelineTest {

        @Rule
        public JUnitSoftAssertions soft = new JUnitSoftAssertions();

        private ProductConfigZipObjectMapper productConfigZipObjectMapper = new ProductConfigZipObjectMapper();
        private ObjectMapper objectMapper = new ObjectMapper();

        @Test
        public void writePipelineExport__WritesTableJson() throws Exception {
            PipelineExport pipelineExport = PipelineExport.builder()
                    .name("pipeline 1")
                    .steps(singletonList(
                            PipelineMapStepExport.builder()
                                    .name("step1")
                                    .description("Step One")
                                    .schemaIn(SchemaReference.builder()
                                            .displayName("schemaIn")
                                            .physicalTableName("SIN")
                                            .version(1)
                                            .build())
                                    .schemaOut(SchemaReference.builder()
                                            .displayName("schemaOut")
                                            .physicalTableName("SNOUT")
                                            .version(1)
                                            .build())
                                    .build()))
                    .build();

            byte[] jsonByteArray = productConfigZipObjectMapper.writePipelineExport(pipelineExport);
            JsonNode jsonNode = objectMapper.readTree(jsonByteArray);


            soft.assertThat(jsonNode.get("name").textValue()).isEqualTo("pipeline 1");
            JsonNode stepNode = jsonNode.get("steps").get(0);

            soft.assertThat(stepNode.get("name").textValue()).isEqualTo("step1");
            soft.assertThat(stepNode.get("description").textValue()).isEqualTo("Step One");
            soft.assertThat(stepNode.get("type").textValue()).isEqualTo("MAP");
            soft.assertThat(stepNode.get("schemaIn").get("displayName").textValue()).isEqualTo("schemaIn");
            soft.assertThat(stepNode.get("schemaIn").get("physicalTableName").textValue()).isEqualTo("SIN");
            soft.assertThat(stepNode.get("schemaIn").get("version").intValue()).isEqualTo(1);
            soft.assertThat(stepNode.get("schemaOut").get("displayName").textValue()).isEqualTo("schemaOut");
            soft.assertThat(stepNode.get("schemaOut").get("physicalTableName").textValue()).isEqualTo("SNOUT");
            soft.assertThat(stepNode.get("schemaOut").get("version").intValue()).isEqualTo(1);
        }
    }
}