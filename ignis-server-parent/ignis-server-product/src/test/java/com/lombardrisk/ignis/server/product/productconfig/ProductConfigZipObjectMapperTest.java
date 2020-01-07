package com.lombardrisk.ignis.server.product.productconfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lombardrisk.ignis.client.external.pipeline.export.PipelineExport;
import com.lombardrisk.ignis.client.external.pipeline.export.SchemaReference;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineMapStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.select.OrderExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.select.SelectExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.select.WindowExport;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport.StringFieldExport;
import com.lombardrisk.ignis.client.external.productconfig.export.ProductConfigExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;
import com.lombardrisk.ignis.client.external.productconfig.export.ValidationRuleExport;
import com.lombardrisk.ignis.server.product.ProductConfigZipObjectMapper;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@RunWith(Enclosed.class)
public class ProductConfigZipObjectMapperTest {

    public static class ProductConfigTest {

        @Rule
        public JUnitSoftAssertions soft = new JUnitSoftAssertions();
        private ProductConfigZipObjectMapper productConfigZipObjectMapper = new ProductConfigZipObjectMapper();
        private ObjectMapper objectMapper = new ObjectMapper();

        @Test
        public void readProductConfigView_ReturnsProductConfigView() throws Exception {
            JsonNode tableJson = objectMapper.createObjectNode()
                    .put("name", "table name")
                    .put("version", 5);

            JsonNode productConfigJson = objectMapper.createObjectNode()
                    .put("id", 123L)
                    .put("name", "new product config view")
                    .put("version", "v1.0")
                    .putPOJO("tables", objectMapper.createArrayNode().addPOJO(tableJson));

            ProductConfigExport productConfigView = productConfigZipObjectMapper.readProductConfigView(
                    objectMapper.writeValueAsBytes(productConfigJson)
            );

            soft.assertThat(productConfigView.getId()).isNull();
            soft.assertThat(productConfigView.getName()).isEqualTo("new product config view");
            soft.assertThat(productConfigView.getVersion()).isEqualTo("v1.0");
            soft.assertThat(productConfigView.getTables()).isNull();
        }
    }

    public static class TableTest {

        @Rule
        public JUnitSoftAssertions soft = new JUnitSoftAssertions();

        private ProductConfigZipObjectMapper productConfigZipObjectMapper = new ProductConfigZipObjectMapper();
        private ObjectMapper objectMapper = new ObjectMapper();

        @Test
        public void readTableView_ReadsTableJson() throws Exception {

            ObjectNode fieldJson = objectMapper.createObjectNode()
                    .put("name", "field name")
                    .put("type", "int");

            ObjectNode ruleJson = objectMapper.createObjectNode()
                    .put("name", "rule name");

            ObjectNode tableJson = objectMapper.createObjectNode()
                    .put("id", 123L)
                    .put("physicalTableName", "table name")
                    .put("displayName", "table display name")
                    .put("startDate", new Date().getTime())
                    .put("createdBy", "admin")
                    .putPOJO("fields", objectMapper.createArrayNode().addPOJO(fieldJson))
                    .put("version", 5)
                    .putPOJO("validationRules", objectMapper.createArrayNode().addPOJO(ruleJson));

            SchemaExport schemaView =
                    productConfigZipObjectMapper.readTableView(objectMapper.writeValueAsBytes(tableJson));

            soft.assertThat(schemaView.getId()).isNull();
            soft.assertThat(schemaView.getPhysicalTableName()).isEqualTo("table name");
            soft.assertThat(schemaView.getDisplayName()).isEqualTo("table display name");
            soft.assertThat(schemaView.getCreatedBy()).isEqualTo("admin");
            soft.assertThat(schemaView.getStartDate()).isInSameDayAs(new Date());
            soft.assertThat(schemaView.getVersion()).isEqualTo(5);
            soft.assertThat(schemaView.getFields()).hasSize(1);
            soft.assertThat(schemaView.getFields().get(0).getName()).isEqualTo("field name");
            soft.assertThat(schemaView.getFields().get(0)).isInstanceOf(FieldExport.IntegerFieldExport.class);
            soft.assertThat(schemaView.getValidationRules().get(0).getName()).isEqualTo("rule name");
        }
    }

    @SuppressWarnings("unchecked")
    public static class FieldTest {

        @Rule
        public JUnitSoftAssertions soft = new JUnitSoftAssertions();

        private ProductConfigZipObjectMapper productConfigZipObjectMapper = new ProductConfigZipObjectMapper();
        private ObjectMapper objectMapper = new ObjectMapper();

        @Test
        public void readTableView_ReadsFieldJson() throws Exception {
            ObjectNode intField = objectMapper.createObjectNode()
                    .put("id", 12345L)
                    .put("name", "int field")
                    .put("type", "int")
                    .put("nullable", true);

            ObjectNode stringField = objectMapper.createObjectNode()
                    .put("id", 98765L)
                    .put("name", "string field")
                    .put("type", "string")
                    .put("nullable", false);

            ObjectNode tableJson = objectMapper.createObjectNode()
                    .putPOJO("fields", objectMapper.createArrayNode().addPOJO(intField).addPOJO(stringField));

            List<FieldExport> fields = productConfigZipObjectMapper
                    .readTableView(objectMapper.writeValueAsBytes(tableJson))
                    .getFields();

            soft.assertThat(fields).hasSize(2);
            soft.assertThat(fields)
                    .extracting(FieldExport::getId, FieldExport::getName, FieldExport::isNullable)
                    .containsExactly(tuple(null, "int field", true), tuple(null, "string field", false));

            soft.assertThat(fields.get(0)).isInstanceOf(FieldExport.IntegerFieldExport.class);
            soft.assertThat(fields.get(1)).isInstanceOf(StringFieldExport.class);
        }
    }

    public static class ValidationRuleTest {

        @Rule
        public JUnitSoftAssertions soft = new JUnitSoftAssertions();

        private ProductConfigZipObjectMapper productConfigZipObjectMapper = new ProductConfigZipObjectMapper();
        private ObjectMapper objectMapper = new ObjectMapper();

        @Test
        public void readTableView_ReadsRuleJson() throws Exception {
            ObjectNode ruleJson = objectMapper.createObjectNode()
                    .put("id", 123L)
                    .put("name", "rule name")
                    .put("ruleId", "rule id")
                    .put("validationRuleType", "SYNTAX")
                    .put("validationRuleSeverity", "CRITICAL")
                    .put("version", 999)
                    .put("startDate", "1066-01-01")
                    .put("endDate", "1067-01-01")
                    .put("description", "rule description")
                    .put("expression", "rule expression");

            ObjectNode tableJson = objectMapper.createObjectNode()
                    .putPOJO("validationRules", objectMapper.createArrayNode().addPOJO(ruleJson));

            SchemaExport schemaView =
                    productConfigZipObjectMapper.readTableView(objectMapper.writeValueAsBytes(tableJson));

            soft.assertThat(schemaView.getValidationRules()).hasSize(1);
            soft.assertThat(schemaView.getValidationRules().get(0).getId())
                    .isEqualTo(null);
            soft.assertThat(schemaView.getValidationRules().get(0).getName())
                    .isEqualTo("rule name");
            soft.assertThat(schemaView.getValidationRules().get(0).getRuleId())
                    .isEqualTo("rule id");
            soft.assertThat(schemaView.getValidationRules().get(0).getValidationRuleType())
                    .isEqualTo(ValidationRuleExport.Type.SYNTAX);
            soft.assertThat(schemaView.getValidationRules().get(0).getValidationRuleSeverity())
                    .isEqualTo(ValidationRuleExport.Severity.CRITICAL);
            soft.assertThat(schemaView.getValidationRules().get(0).getVersion())
                    .isEqualTo(999);
            soft.assertThat(schemaView.getValidationRules().get(0).getStartDate())
                    .isEqualTo(LocalDate.of(1066, 1, 1));
            soft.assertThat(schemaView.getValidationRules().get(0).getEndDate())
                    .isEqualTo(LocalDate.of(1067, 1, 1));
            soft.assertThat(schemaView.getValidationRules().get(0).getDescription())
                    .isEqualTo("rule description");
            soft.assertThat(schemaView.getValidationRules().get(0).getExpression())
                    .isEqualTo("rule expression");
        }
    }

    public static class PipelineTest {

        @Rule
        public JUnitSoftAssertions soft = new JUnitSoftAssertions();

        private ProductConfigZipObjectMapper productConfigZipObjectMapper = new ProductConfigZipObjectMapper();
        private ObjectMapper objectMapper = new ObjectMapper();

        @Test
        public void readPipelineView_ReadsJson() throws Exception {
            ArrayNode steps = objectMapper.createArrayNode();

            ObjectNode selectNode1 = objectMapper.createObjectNode();
            selectNode1.put("select", "A");
            selectNode1.put("outputFieldName", "ABC");
            selectNode1.put("isWindow", false);

            ObjectNode windowNode = objectMapper.createObjectNode();
            windowNode.set("partitionBy", objectMapper.createArrayNode()
                    .add("C").add("D"));
            windowNode.set("orderBy", objectMapper.createArrayNode().add(
                    objectMapper.createObjectNode()
                            .put("fieldName", "D")
                            .put("direction", "DESC")
                            .put("priority", 1)));

            ObjectNode selectNode2 = objectMapper.createObjectNode();
            selectNode2.put("select", "B");
            selectNode2.put("outputFieldName", "XYZ");
            selectNode2.put("isWindow", true);
            selectNode2.set("window", windowNode);

            ObjectNode stepNode = objectMapper.createObjectNode()
                    .put("name", "step")
                    .put("description", "description")
                    .put("type", "MAP");
            stepNode.set("selects", objectMapper.createArrayNode().add(selectNode1).add(selectNode2));
            stepNode.set("schemaIn", objectMapper.createObjectNode()
                    .put("physicalTableName", "physicalTableName1")
                    .put("displayName", "displayName1")
                    .put("version", 1));
            stepNode.set("schemaOut", objectMapper.createObjectNode()
                    .put("physicalTableName", "physicalTableName2")
                    .put("displayName", "displayName2")
                    .put("version", 2));

            steps.add(stepNode);

            JsonNode pipelineJson = objectMapper.createObjectNode()
                    .put("name", "name")
                    .set("steps", steps);

            PipelineExport pipelineExport =
                    productConfigZipObjectMapper.readPipelineView(objectMapper.writeValueAsBytes(pipelineJson));

            soft.assertThat(pipelineExport.getName())
                    .isEqualTo("name");

            assertThat(pipelineExport.getSteps())
                    .hasSize(1);

            PipelineStepExport stepExport = pipelineExport.getSteps().iterator().next();
            soft.assertThat(stepExport.getName())
                    .isEqualTo("step");
            soft.assertThat(stepExport.getDescription())
                    .isEqualTo("description");
            soft.assertThat(stepExport.getType())
                    .isEqualTo(TransformationType.MAP);

            PipelineMapStepExport mapStepExport = (PipelineMapStepExport) stepExport;
            soft.assertThat(mapStepExport.getSelects())
                    .containsExactlyInAnyOrder(
                            SelectExport.builder()
                                    .select("A").outputFieldName("ABC").isWindow(false).window(null).build(),
                            SelectExport.builder()
                                    .select("B").outputFieldName("XYZ").isWindow(true)
                                    .window(WindowExport.builder()
                                            .partitionBy(newHashSet("C", "D"))
                                            .orderBy(singletonList(OrderExport.builder()
                                                    .fieldName("D")
                                                    .direction("DESC")
                                                    .priority(1)
                                                    .build()))
                                            .build())
                                    .build());
            soft.assertThat(mapStepExport.getSchemaIn())
                    .isEqualTo(SchemaReference.builder()
                            .displayName("displayName1")
                            .physicalTableName("physicalTableName1")
                            .version(1)
                            .build());
            soft.assertThat(mapStepExport.getSchemaOut())
                    .isEqualTo(SchemaReference.builder()
                            .displayName("displayName2")
                            .physicalTableName("physicalTableName2")
                            .version(2)
                            .build());
        }
    }
}
