package com.lombardrisk.ignis.server.product.pipeline;

import com.lombardrisk.ignis.client.external.pipeline.export.PipelineExport;
import com.lombardrisk.ignis.client.external.pipeline.export.SchemaReference;
import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinFieldExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineAggregationStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineJoinStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineMapStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineScriptletStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineUnionStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineWindowStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.ScriptletInputExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.UnionExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.select.SelectExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.select.WindowExport;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.common.stream.CollectorUtils;
import com.lombardrisk.ignis.common.stream.SeqUtils;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.failure.NotFoundByFailure;
import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineAggregationStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineJoinStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineMapStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineScriptletStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineUnionStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineWindowStep;
import com.lombardrisk.ignis.server.product.pipeline.select.Order;
import com.lombardrisk.ignis.server.product.pipeline.select.PipelineFilter;
import com.lombardrisk.ignis.server.product.pipeline.select.Select;
import com.lombardrisk.ignis.server.product.pipeline.select.Union;
import com.lombardrisk.ignis.server.product.pipeline.select.Window;
import com.lombardrisk.ignis.server.product.pipeline.transformation.Join;
import com.lombardrisk.ignis.server.product.pipeline.transformation.JoinField;
import com.lombardrisk.ignis.server.product.pipeline.transformation.ScriptletInput;
import com.lombardrisk.ignis.server.product.pipeline.validation.PipelineImportBeanValidator;
import com.lombardrisk.ignis.server.product.table.model.Field;
import com.lombardrisk.ignis.server.product.table.model.Table;
import io.vavr.Tuple;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.common.MapperUtils.mapCollectionOrEmpty;
import static com.lombardrisk.ignis.common.stream.CollectionUtils.orEmpty;
import static com.lombardrisk.ignis.data.common.Versionable.versionKey;
import static io.vavr.Function1.identity;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;

@Slf4j
public class PipelineImportService {

    private final PipelineImportBeanValidator pipelineImportValidator;
    private final PipelineService pipelineService;

    public PipelineImportService(
            final PipelineImportBeanValidator pipelineImportValidator,
            final PipelineService pipelineService) {
        this.pipelineImportValidator = pipelineImportValidator;
        this.pipelineService = pipelineService;
    }

    public Validation<List<ErrorResponse>, List<Pipeline>> importProductPipelines(
            final List<PipelineExport> pipelineExports, final Long productConfigId, final Set<Table> tables) {

        List<ErrorResponse> beanValidationFailures = pipelineImportValidator.validate(pipelineExports);
        if (!beanValidationFailures.isEmpty()) {
            return Validation.invalid(beanValidationFailures);
        }

        Map<String, Table> schemaByVersionName = tables.stream()
                .collect(toMap(Table::getVersionedName, Function.identity()));

        Validation<List<CRUDFailure>, List<Pipeline>> result = validateAndBuildPipelines(
                pipelineExports, schemaByVersionName, productConfigId);

        if (result.isValid()) {
            List<Pipeline> pipelines = pipelineService.savePipelines(result.get());
            return Validation.valid(pipelines);
        }

        return Validation.invalid(
                MapperUtils.map(result.getError(), CRUDFailure::toErrorResponse));
    }

    private Validation<List<CRUDFailure>, List<Pipeline>> validateAndBuildPipelines(
            final List<PipelineExport> pipelineExports,
            final Map<String, Table> schemaByVersionName,
            final Long productConfigId) {

        List<Pipeline> pipelines = new ArrayList<>();
        List<CRUDFailure> errors = new ArrayList<>();

        for (PipelineExport pipelineExport : pipelineExports) {
            String pipelineName = pipelineExport.getName();
            if (pipelineService.existsByName(pipelineName)) {
                log.info("Pipeline {} already exists, ignoring pipeline", pipelineName);
                continue;
            }

            Validation<List<CRUDFailure>, List<PipelineStep>> result = findSchemasAndCreateSteps(
                    schemaByVersionName, pipelineExport);

            if (result.isInvalid()) {
                errors.addAll(result.getError());
            } else {
                Pipeline pipeline = Pipeline.builder()
                        .productId(productConfigId)
                        .name(pipelineName)
                        .steps(newLinkedHashSet(result.get()))
                        .build();

                pipelines.add(pipeline);
            }
        }

        return errors.isEmpty()
                ? Validation.valid(pipelines)
                : Validation.invalid(errors);
    }

    private Validation<List<CRUDFailure>, List<PipelineStep>> findSchemasAndCreateSteps(
            final Map<String, Table> schemaByVersionName,
            final PipelineExport pipelineExport) {

        return pipelineExport.getSteps().stream()
                .map(step -> validateStep(schemaByVersionName, step))
                .collect(CollectorUtils.groupCollectionValidations());
    }

    private Validation<List<CRUDFailure>, PipelineStep> validateStep(
            final Map<String, Table> schemaByVersionName,
            final PipelineStepExport stepExport) {
        switch (stepExport.getType()) {
            case AGGREGATION:
                return validateAggregationStep(schemaByVersionName, (PipelineAggregationStepExport) stepExport);
            case MAP:
                return validateMapStep(schemaByVersionName, (PipelineMapStepExport) stepExport);
            case JOIN:
                return validateJoinStep(schemaByVersionName, (PipelineJoinStepExport) stepExport);
            case WINDOW:
                return validateWindowStep(schemaByVersionName, (PipelineWindowStepExport) stepExport);
            case UNION:
                return validateUnionStep(schemaByVersionName, (PipelineUnionStepExport) stepExport);
            case SCRIPTLET:
                return validateScriptletStep(schemaByVersionName, (PipelineScriptletStepExport) stepExport);
            default:
                throw new NotImplementedException(stepExport.getType() + " not supported yet");
        }
    }

    private Validation<List<CRUDFailure>, PipelineStep> validateMapStep(
            final Map<String, Table> schemaByVersionName,
            final PipelineMapStepExport stepExport) {

        Validation<List<CRUDFailure>, Table> schemaInValidation =
                findSchema(stepExport.getSchemaIn(), schemaByVersionName);

        Validation<List<CRUDFailure>, Table> schemaOutValidation =
                findSchema(stepExport.getSchemaOut(), schemaByVersionName);

        Validation<List<CRUDFailure>, Set<Select>> selectsValidation =
                schemaOutValidation.flatMap(schema -> validateSelects(stepExport.getSelects(), schema));

        return Validation.combine(schemaInValidation, schemaOutValidation, selectsValidation)
                .ap((schemaIn, schemaOut, selects) -> createMapStep(stepExport, schemaIn, schemaOut, selects))
                .mapError(SeqUtils::flatMapToList);
    }

    private PipelineStep createMapStep(
            final PipelineMapStepExport stepExport,
            final Table schemaIn,
            final Table schemaOut,
            final Set<Select> selects) {

        return PipelineMapStep.builder()
                .name(stepExport.getName())
                .description(stepExport.getDescription())
                .schemaInId(schemaIn.getId())
                .schemaOutId(schemaOut.getId())
                .selects(selects)
                .filters(mapCollectionOrEmpty(stepExport.getFilters(), identity(), LinkedHashSet::new))
                .build();
    }

    private Validation<List<CRUDFailure>, PipelineStep> validateAggregationStep(
            final Map<String, Table> schemaByVersionName,
            final PipelineAggregationStepExport stepExport) {

        Validation<List<CRUDFailure>, Table> schemaInValidation =
                findSchema(stepExport.getSchemaIn(), schemaByVersionName);

        Validation<List<CRUDFailure>, Table> schemaOutValidation =
                findSchema(stepExport.getSchemaOut(), schemaByVersionName);

        Validation<List<CRUDFailure>, Set<Select>> selectsValidation =
                schemaOutValidation.flatMap(schema -> validateSelects(stepExport.getSelects(), schema));

        return Validation.combine(schemaInValidation, schemaOutValidation, selectsValidation)
                .ap((schemaIn, schemaOut, selects) -> createAggregationStep(stepExport, schemaIn, schemaOut, selects))
                .mapError(SeqUtils::flatMapToList);
    }

    private PipelineStep createAggregationStep(
            final PipelineAggregationStepExport stepExport,
            final Table schemaIn,
            final Table schemaOut,
            final Set<Select> selects) {

        return PipelineAggregationStep.builder()
                .name(stepExport.getName())
                .description(stepExport.getDescription())
                .schemaInId(schemaIn.getId())
                .schemaOutId(schemaOut.getId())
                .selects(selects)
                .filters(mapCollectionOrEmpty(stepExport.getFilters(), identity(), LinkedHashSet::new))
                .groupings(mapCollectionOrEmpty(stepExport.getGroupings(), x -> x, LinkedHashSet::new))
                .build();
    }

    private Validation<List<CRUDFailure>, PipelineStep> validateJoinStep(
            final Map<String, Table> schemaByVersionName,
            final PipelineJoinStepExport stepExport) {

        Validation<List<CRUDFailure>, List<Join>> joinValidationResult = stepExport.getJoins().stream()
                .map(joinExport -> validateJoinExport(schemaByVersionName, joinExport))
                .collect(CollectorUtils.groupCollectionValidations());

        Validation<List<CRUDFailure>, Table> schemaOutValidation =
                findSchema(stepExport.getSchemaOut(), schemaByVersionName);

        Validation<List<CRUDFailure>, Set<Select>> selectsValidation =
                schemaOutValidation.flatMap(schema -> validateSelects(stepExport.getSelects(), schema));

        return Validation.combine(joinValidationResult, schemaOutValidation, selectsValidation)
                .ap((joins, schemaOut, selects) -> createJoinStep(stepExport, joins, schemaOut, selects))
                .mapError(SeqUtils::flatMapToList);
    }

    private PipelineStep createJoinStep(
            final PipelineJoinStepExport stepExport,
            final List<Join> joins,
            final Table schemaOut,
            final Set<Select> selects) {

        return PipelineJoinStep.builder()
                .name(stepExport.getName())
                .description(stepExport.getDescription())
                .schemaOutId(schemaOut.getId())
                .selects(selects)
                .joins(newLinkedHashSet(joins))
                .build();
    }

    private Validation<List<CRUDFailure>, Join> validateJoinExport(
            final Map<String, Table> schemaByVersionName, final JoinExport joinExport) {

        Validation<List<CRUDFailure>, Table> leftSchema = findSchema(joinExport.getLeft(), schemaByVersionName);
        Validation<List<CRUDFailure>, Table> rightSchema = findSchema(joinExport.getRight(), schemaByVersionName);

        return Validation.combine(leftSchema, rightSchema)
                .ap(Tuple::of)
                .mapError(SeqUtils::flatMapToList)
                .flatMap(tables -> validateJoinExport(tables._1, tables._2, joinExport));
    }

    private Validation<List<CRUDFailure>, Join> validateJoinExport(
            final Table leftSchema, final Table rightSchema, final JoinExport joinExport) {

        return validateJoinFieldExports(leftSchema, rightSchema, joinExport.getJoinFields())
                .map(joinFields -> Join.builder()
                        .leftSchemaId(leftSchema.getId())
                        .rightSchemaId(rightSchema.getId())
                        .joinType(Join.JoinType.valueOf(joinExport.getType().name()))
                        .joinFields(joinFields)
                        .build());
    }

    private Validation<List<CRUDFailure>, Set<JoinField>> validateJoinFieldExports(
            final Table leftSchema, final Table rightSchema, final List<JoinFieldExport> joinFieldExports) {

        return joinFieldExports.stream()
                .map(fieldExport -> validateJoinFieldExport(leftSchema, rightSchema, fieldExport))
                .collect(CollectorUtils.groupCollectionValidations())
                .map(HashSet::new);
    }

    private Validation<List<CRUDFailure>, JoinField> validateJoinFieldExport(
            final Table leftSchema, final Table rightSchema, final JoinFieldExport joinFieldExport) {

        Validation<CRUDFailure, Field> leftField = findFieldInSchema(leftSchema, joinFieldExport.getLeftColumn());
        Validation<CRUDFailure, Field> rightField = findFieldInSchema(rightSchema, joinFieldExport.getRightColumn());

        return Validation.combine(leftField, rightField)
                .ap((left, right) -> JoinField.builder()
                        .leftJoinFieldId(left.getId())
                        .rightJoinFieldId(right.getId())
                        .build())
                .mapError(Seq::toJavaList);
    }

    private Validation<List<CRUDFailure>, PipelineStep> validateWindowStep(
            final Map<String, Table> schemaByVersionName,
            final PipelineWindowStepExport stepExport) {

        Validation<List<CRUDFailure>, Table> schemaInValidation =
                findSchema(stepExport.getSchemaIn(), schemaByVersionName);

        Validation<List<CRUDFailure>, Table> schemaOutValidation =
                findSchema(stepExport.getSchemaOut(), schemaByVersionName);

        Validation<List<CRUDFailure>, Set<Select>> selectsValidation =
                schemaOutValidation.flatMap(schema -> validateSelects(stepExport.getSelects(), schema));

        return Validation.combine(schemaInValidation, schemaOutValidation, selectsValidation)
                .ap((schemaIn, schemaOut, selects) -> createWindowStep(stepExport, schemaIn, schemaOut, selects))
                .mapError(SeqUtils::flatMapToList);
    }

    private PipelineStep createWindowStep(
            final PipelineWindowStepExport stepExport,
            final Table schemaIn,
            final Table schemaOut,
            final Set<Select> selects) {

        return PipelineWindowStep.builder()
                .name(stepExport.getName())
                .description(stepExport.getDescription())
                .schemaInId(schemaIn.getId())
                .schemaOutId(schemaOut.getId())
                .selects(selects)
                .filters(newLinkedHashSet(orEmpty(stepExport.getFilters())))
                .build();
    }

    private Validation<List<CRUDFailure>, PipelineStep> validateUnionStep(
            final Map<String, Table> schemaByVersionName,
            final PipelineUnionStepExport stepExport) {

        Validation<List<CRUDFailure>, Table> schemaOutValidation =
                findSchema(stepExport.getSchemaOut(), schemaByVersionName);

        if (schemaOutValidation.isInvalid()) {
            return Validation.invalid(schemaOutValidation.getError());
        }

        List<CRUDFailure> errors = new ArrayList<>();
        Set<Select> unionSelects = new LinkedHashSet<>();
        Set<PipelineFilter> unionFilters = new LinkedHashSet<>();
        Set<Long> inputSchemaIds = new HashSet<>();

        for (UnionExport union : stepExport.getUnions()) {
            Validation<List<CRUDFailure>, Table> findInputTable =
                    findSchema(union.getUnionInSchema(), schemaByVersionName);

            if (findInputTable.isInvalid()) {
                errors.addAll(findInputTable.getError());
                continue;
            }

            Table inputTable = findInputTable.get();
            inputSchemaIds.add(inputTable.getId());

            Validation<List<CRUDFailure>, Set<Select>> selectValidation =
                    validateSelects(union.getSelects(), schemaOutValidation.get());
            if (selectValidation.isInvalid()) {
                errors.addAll(selectValidation.getError());
                continue;
            }

            Set<Select> schemaSelects = selectValidation.get();
            schemaSelects.forEach(select -> {
                select.setUnion(true);
                select.setSelectUnion(Union.forSchema(inputTable.getId()));
                unionSelects.add(select);
            });

            List<String> filters = union.getFilters() == null ? emptyList() : union.getFilters();
            for (String filter : filters) {
                unionFilters.add(PipelineFilter.builder()
                        .filter(filter)
                        .unionSchemaId(inputTable.getId())
                        .build());
            }
        }

        if (!errors.isEmpty()) {
            return Validation.invalid(errors);
        }

        return Validation.valid(PipelineUnionStep.builder()
                .name(stepExport.getName())
                .description(stepExport.getDescription())
                .schemaInIds(inputSchemaIds)
                .schemaOutId(schemaOutValidation.get().getId())
                .selects(unionSelects)
                .filters(unionFilters)
                .build());
    }

    private Validation<List<CRUDFailure>, PipelineStep> validateScriptletStep(
            final Map<String, Table> schemaByVersionName,
            final PipelineScriptletStepExport stepExport) {

        Validation<List<CRUDFailure>, List<ScriptletInput>> scriptletInputs = stepExport.getSchemasIn().stream()
                .map(scriptletInput -> validateScriptletInput(schemaByVersionName, scriptletInput))
                .collect(CollectorUtils.groupCollectionValidations());

        Validation<List<CRUDFailure>, Table> scriptletOutput =
                findSchema(stepExport.getOutputSchema(), schemaByVersionName);

        return Validation.combine(scriptletInputs, scriptletOutput)
                .ap((inputs, output) -> createScriptletStep(stepExport, inputs, output))
                .mapError(SeqUtils::flatMapToList);
    }

    private Validation<List<CRUDFailure>, ScriptletInput> validateScriptletInput(
            final Map<String, Table> schemaByVersionName, final ScriptletInputExport scriptletInputExport) {

        return findSchema(scriptletInputExport.getInputSchema(), schemaByVersionName)
                .map(table -> ScriptletInput.builder()
                        .inputName(scriptletInputExport.getInputName())
                        .schemaInId(table.getId())
                        .build());
    }

    private PipelineStep createScriptletStep(
            final PipelineScriptletStepExport stepExport, final List<ScriptletInput> inputs, final Table output) {

        return PipelineScriptletStep.builder()
                .name(stepExport.getName())
                .description(stepExport.getDescription())
                .jarFile(stepExport.getJarFile())
                .className(stepExport.getClassName())
                .schemaIns(new HashSet<>(inputs))
                .schemaOutId(output.getId())
                .build();
    }

    private Validation<List<CRUDFailure>, Table> findSchema(
            final SchemaReference schemaReference, final Map<String, Table> schemaByVersionName) {

        Table table = schemaByVersionName.get(
                versionKey(schemaReference.getPhysicalTableName(), schemaReference.getVersion()));

        return Option.of(table).toValid(singletonList(cannotFindSchema(schemaReference)));
    }

    private Validation<CRUDFailure, Field> findFieldInSchema(final Table table, final String fieldName) {
        return Option.ofOptional(table.getFields().stream()
                .filter(field -> field.getName().equals(fieldName))
                .findFirst())
                .toValidation(cannotFindField(table.getDisplayName(), fieldName));
    }

    private Validation<List<CRUDFailure>, Set<Select>> validateSelects(
            final List<SelectExport> selectExports, final Table schemaOut) {

        return selectExports.stream()
                .map(selectExport -> validateSelect(selectExport, schemaOut))
                .collect(CollectorUtils.groupValidations())
                .map(LinkedHashSet::new);
    }

    private Validation<CRUDFailure, Select> validateSelect(final SelectExport selectExport, final Table schemaOut) {
        Optional<Field> schemaOutField = schemaOut.getFields().stream()
                .filter(field -> field.getName().equals(selectExport.getOutputFieldName()))
                .findFirst();

        return Option.ofOptional(schemaOutField)
                .map(field -> Select.builder()
                        .select(selectExport.getSelect())
                        .outputFieldId(field.getId())
                        .order(selectExport.getOrder())
                        .isIntermediateResult(selectExport.isIntermediateResult())
                        .isWindow(selectExport.getIsWindow())
                        .window(selectExport.getWindow() == null ? null : convertWindowExport(selectExport.getWindow()))
                        .build())
                .toValid(cannotFindField(schemaOut.getDisplayName(), selectExport.getOutputFieldName()));
    }

    private Window convertWindowExport(final WindowExport windowExport) {
        return Window.builder()
                .partitions(windowExport.getPartitionBy())
                .orders(mapCollectionOrEmpty(
                        windowExport.getOrderBy(),
                        order -> Order.builder()
                                .fieldName(order.getFieldName())
                                .direction(Order.Direction.valueOf(order.getDirection()))
                                .priority(order.getPriority())
                                .build(),
                        LinkedHashSet::new))
                .build();
    }

    private NotFoundByFailure cannotFindSchema(final SchemaReference schema) {
        return CRUDFailure.cannotFind("schema")
                .with("displayName", schema.getDisplayName())
                .with("physicalTableName", schema.getPhysicalTableName())
                .with("version", schema.getVersion())
                .asFailure();
    }

    private NotFoundByFailure cannotFindField(final String schemaName, final String columnName) {
        return CRUDFailure.cannotFind("field")
                .with("schema", schemaName)
                .with("name", columnName)
                .asFailure();
    }
}
