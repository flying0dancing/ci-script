package com.lombardrisk.ignis.design.server.pipeline;

import com.lombardrisk.ignis.client.design.pipeline.SyntaxCheckRequest;
import com.lombardrisk.ignis.client.design.pipeline.error.SelectResult;
import com.lombardrisk.ignis.client.design.pipeline.error.StepExecutionResult;
import com.lombardrisk.ignis.common.stream.CollectionUtils;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.pipeline.model.ExecuteStepContext;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Select;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import com.lombardrisk.ignis.design.server.productconfig.schema.SchemaService;
import io.vavr.Value;
import io.vavr.control.Validation;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.lombardrisk.ignis.design.field.model.Field.FIELD_NAME;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@Slf4j
public class PipelineStepSelectsValidator {

    private static final LocalDate DEFAULT_REFERENCE_DATE = LocalDate.of(1970, 1, 1);
    private final SchemaService schemaService;
    private final PipelineStepExecutor pipelineStepExecutor;
    private final SparkUDFService sparkUDFService;

    public PipelineStepSelectsValidator(
            final SchemaService schemaService,
            final PipelineStepExecutor pipelineStepExecutor,
            final SparkUDFService sparkUDFService) {
        this.schemaService = schemaService;
        this.pipelineStepExecutor = pipelineStepExecutor;
        this.sparkUDFService = sparkUDFService;
    }

    public StepExecutionResult validate(final PipelineStep step) {
        sparkUDFService.registerUdfs(DEFAULT_REFERENCE_DATE);
        return validatePipelineStep(step, false);
    }

    public StepExecutionResult validateWithIndividualErrors(final PipelineStep step) {
        sparkUDFService.registerUdfs(DEFAULT_REFERENCE_DATE);
        return validatePipelineStep(step, true);
    }

    public Validation<List<ErrorResponse>, SelectResult> checkSyntax(
            final PipelineStep step,
            final SyntaxCheckRequest syntaxCheckRequest) {

        sparkUDFService.registerUdfs(DEFAULT_REFERENCE_DATE);
        return validateSchemasForPipelineStep(step)
                .ap((inputSchemas, schema) -> checkSyntaxForSingleSelect(
                        step,
                        syntaxCheckRequest,
                        inputSchemas,
                        schema))
                .mapError(Value::toJavaList)
                .mapError(CRUDFailure::toErrorResponses)
                .flatMap(selectResults -> selectResults);
    }

    private Validation.Builder<CRUDFailure, List<Schema>, Schema> validateSchemasForPipelineStep(final PipelineStep step) {
        Validation<CRUDFailure, List<Schema>> inputSchemasValidation =
                schemaService.findOrIdsNotFound(step.getInputs());

        Validation<CRUDFailure, Schema> outputSchemaValidation =
                schemaService.findWithValidation(step.getOutput());

        return Validation.combine(inputSchemasValidation, outputSchemaValidation);
    }

    private Validation<List<ErrorResponse>, SelectResult> checkSyntaxForSingleSelect(
            final PipelineStep step,
            final SyntaxCheckRequest syntaxCheckRequest,
            final List<Schema> inputSchemas,
            final Schema outputSchema) {
        Optional<Field> optionalField = outputSchema.getFields().stream()
                .filter(field -> field.getId().equals(syntaxCheckRequest.getOutputFieldId()))
                .findFirst();

        if (!optionalField.isPresent()) {
            return Validation.invalid(singletonList(
                    CRUDFailure.notFoundIds(FIELD_NAME, syntaxCheckRequest.getOutputFieldId())
                            .toErrorResponse()));
        }

        Optional<Select> selectOptional = step.getSelects().stream()
                .filter(select -> select.getOutputFieldId().equals(syntaxCheckRequest.getOutputFieldId()))
                .findFirst();

        if (!selectOptional.isPresent()) {
            return Validation.invalid(singletonList(CRUDFailure.invalidRequestParameter(
                    "OutputFieldId",
                    syntaxCheckRequest.getOutputFieldId()).toErrorResponse()));
        }

        Select select = selectOptional.get();
        select.setSelect(syntaxCheckRequest.getSparkSql());
        return executeSingleSelect(step, inputSchemas, outputSchema, optionalField.get(), select);
    }

    private Validation<List<ErrorResponse>, SelectResult> executeSingleSelect(
            final PipelineStep step,
            final List<Schema> inputSchemas,
            final Schema outputSchema,
            final Field outputField,
            final Select select) {

        Set<Select> intermediateSelects = step.getSelects().stream()
                .filter(Select::isIntermediate)
                .sorted(Select.selectOrder())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        step.setSelects(CollectionUtils.concat(intermediateSelects, select, LinkedHashSet::new));

        ExecuteStepContext executeStepContext = ExecuteStepContext.mockData(inputSchemas, outputSchema);

        List<SelectResult> selectResults = pipelineStepExecutor
                .transformOutputFieldsIndividually(step, executeStepContext)
                .stream()
                .filter(selectResult -> !selectResult.isValid())
                .collect(toList());

        SelectResult lastSelect = selectResults.isEmpty() ? null : selectResults.get(selectResults.size() - 1);
        return Validation.valid(lastSelect);
    }

    private StepExecutionResult validatePipelineStep(
            final PipelineStep step,
            final boolean executeSelectsIndividually) {
        return validateSchemasForPipelineStep(step)
                .ap((inputSchemas, outputSchema) -> executeStep(step, inputSchemas, outputSchema))
                .fold(
                        crudFailures -> StepExecutionResult.builder()
                                .errors(CRUDFailure.toErrorResponses(crudFailures.toJavaList()))
                                .build(),
                        Function.identity());
    }

    private StepExecutionResult executeStep(
            final PipelineStep step,
            final List<Schema> inputSchemas,
            final Schema outputSchema) {

        ExecuteStepContext executeStepContext = ExecuteStepContext.mockData(inputSchemas, outputSchema);
        Validation<List<ErrorResponse>, Dataset<Row>> execute = pipelineStepExecutor.executeFullTransformation(
                step, executeStepContext);
        StepExecutionResult.StepExecutionResultBuilder resultBuilder = StepExecutionResult.builder();

        if (execute.isInvalid()) {
            resultBuilder.errors(execute.getError());
        }

        List<SelectResult> selectResults = pipelineStepExecutor
                .transformOutputFieldsIndividually(step, executeStepContext)
                .stream()
                .filter(selectResult -> !selectResult.isValid())
                .collect(toList());

        resultBuilder.selectsExecutionErrors(StepExecutionResult.SelectsExecutionErrors.builder()
                .individualErrors(selectResults)
                .build());

        return resultBuilder.build();
    }
}
