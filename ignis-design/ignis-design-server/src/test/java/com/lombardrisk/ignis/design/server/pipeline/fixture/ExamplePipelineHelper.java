package com.lombardrisk.ignis.design.server.pipeline.fixture;

import com.lombardrisk.ignis.client.design.pipeline.PipelineStepView;
import com.lombardrisk.ignis.client.design.pipeline.PipelineView;
import com.lombardrisk.ignis.client.design.pipeline.select.SelectRequest;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.design.field.FieldService;
import com.lombardrisk.ignis.design.server.fixtures.Design;
import com.lombardrisk.ignis.design.server.pipeline.PipelineService;
import com.lombardrisk.ignis.design.server.pipeline.PipelineStepService;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import com.lombardrisk.ignis.design.server.productconfig.schema.SchemaService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.dateFieldRequest;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.intFieldRequest;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.longFieldRequest;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.stringFieldRequest;

@AllArgsConstructor
public class ExamplePipelineHelper {

    private final SchemaService schemaService;
    private final FieldService fieldService;
    private final PipelineService pipelineService;
    private final PipelineStepService pipelineStepService;

    public FullNameMapStepConfig fullNameMapConfig() {
        PipelineView pipeline = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Design.Populated.createPipelineRequest()
                        .name("PipelineOne")
                        .build()))
                .getResult();

        Schema a = schemaService.createNew(Design.Populated.schema("a").build());

        FieldDto firstNameField = fieldService.save(a.getId(), stringFieldRequest("FIRST_NAME").build())
                .get();
        FieldDto lastNameField = fieldService.save(a.getId(), stringFieldRequest("LAST_NAME").build())
                .get();

        Schema b = schemaService.createNew(Design.Populated.schema("b").build());

        FieldDto fullNameField = fieldService.save(b.getId(), stringFieldRequest("FULL_NAME").build())
                .get();

        PipelineStepView pipelineStepView = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipeline.getId(), Design.Populated.pipelineMapStepRequest()
                        .schemaInId(a.getId())
                        .schemaOutId(b.getId())
                        .selects(newHashSet(SelectRequest.builder()
                                .select("CONCAT(FIRST_NAME, \" \", LAST_NAME)")
                                .outputFieldId(fullNameField.getId())
                                .build()))
                        .build()))
                .getResult();

        return FullNameMapStepConfig.builder()
                .inputSchema(a)
                .inputFirstNameField(firstNameField)
                .inputLastNameField(lastNameField)
                .outputSchema(b)
                .outputFullNameField(fullNameField)
                .pipeline(pipeline)
                .pipelineStep(pipelineStepView)
                .build();
    }

    public OutputMapStepConfig intOutputMapConfig() {
        PipelineView pipeline = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Design.Populated.createPipelineRequest()
                        .name("PipelineTwo")
                        .build()))
                .getResult();

        Schema a = schemaService.createNew(Design.Populated.schema("a").build());

        FieldDto intInputField = fieldService.save(a.getId(), intFieldRequest("INT_INPUT").build())
                .get();

        Schema b = schemaService.createNew(Design.Populated.schema("b").build());

        FieldDto intOutputField = fieldService.save(b.getId(), intFieldRequest("INT_OUTPUT").build())
                .get();

        PipelineStepView pipelineStepView = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipeline.getId(), Design.Populated.pipelineMapStepRequest()
                        .schemaInId(a.getId())
                        .schemaOutId(b.getId())
                        .selects(newHashSet(SelectRequest.builder()
                                .select("0")
                                .outputFieldId(intOutputField.getId())
                                .build()))
                        .build()))
                .getResult();

        return OutputMapStepConfig.builder()
                .inputSchema(a)
                .inputField(intInputField)
                .outputSchema(b)
                .outputField(intOutputField)
                .pipeline(pipeline)
                .pipelineStep(pipelineStepView)
                .build();
    }

    public OutputMapStepConfig dateOutputMapConfig() {
        PipelineView pipeline = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Design.Populated.createPipelineRequest()
                        .name("PipelineThree")
                        .build()))
                .getResult();

        Schema a = schemaService.createNew(Design.Populated.schema("a").build());

        FieldDto dateInputField = fieldService.save(a.getId(), dateFieldRequest("DATE_INPUT").build())
                .get();

        Schema b = schemaService.createNew(Design.Populated.schema("b").build());

        FieldDto dateOutputField = fieldService.save(b.getId(), dateFieldRequest("DATE_OUTPUT").build())
                .get();

        PipelineStepView pipelineStepView = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipeline.getId(), Design.Populated.pipelineMapStepRequest()
                        .schemaInId(a.getId())
                        .schemaOutId(b.getId())
                        .selects(newHashSet(SelectRequest.builder()
                                .select("DATE_INPUT")
                                .outputFieldId(dateOutputField.getId())
                                .build()))
                        .build()))
                .getResult();

        return OutputMapStepConfig.builder()
                .inputSchema(a)
                .inputField(dateInputField)
                .outputSchema(b)
                .outputField(dateOutputField)
                .pipeline(pipeline)
                .pipelineStep(pipelineStepView)
                .build();
    }

    public OutputMapStepConfig longOutputMapConfig() {
        PipelineView pipeline = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Design.Populated.createPipelineRequest()
                        .name("PipelineFour")
                        .build()))
                .getResult();

        Schema a = schemaService.createNew(Design.Populated.schema("a").build());

        FieldDto inputField = fieldService.save(a.getId(), longFieldRequest("LONG_INPUT").build())
                .get();

        Schema b = schemaService.createNew(Design.Populated.schema("b").build());

        FieldDto outputField = fieldService.save(b.getId(), longFieldRequest("LONG_OUTPUT").build())
                .get();

        PipelineStepView pipelineStepView = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipeline.getId(), Design.Populated.pipelineMapStepRequest()
                        .schemaInId(a.getId())
                        .schemaOutId(b.getId())
                        .selects(newHashSet(SelectRequest.builder()
                                .select("0")
                                .outputFieldId(outputField.getId())
                                .build()))
                        .build()))
                .getResult();

        return OutputMapStepConfig.builder()
                .inputSchema(a)
                .inputField(inputField)
                .outputSchema(b)
                .outputField(outputField)
                .pipeline(pipeline)
                .pipelineStep(pipelineStepView)
                .build();
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class FullNameMapStepConfig {

        private final Schema inputSchema;
        private final Schema outputSchema;
        private final FieldDto inputFirstNameField;
        private final FieldDto inputLastNameField;
        private final FieldDto outputFullNameField;
        private final PipelineView pipeline;
        private final PipelineStepView pipelineStep;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class OutputMapStepConfig {

        private final Schema inputSchema;
        private final Schema outputSchema;
        private final FieldDto inputField;
        private final FieldDto outputField;
        private final PipelineView pipeline;
        private final PipelineStepView pipelineStep;
    }
}
