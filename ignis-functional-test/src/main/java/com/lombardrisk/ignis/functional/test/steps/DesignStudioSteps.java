package com.lombardrisk.ignis.functional.test.steps;

import com.lombardrisk.ignis.client.core.view.IdView;
import com.lombardrisk.ignis.client.design.pipeline.CreatePipelineRequest;
import com.lombardrisk.ignis.client.design.pipeline.PipelineClient;
import com.lombardrisk.ignis.client.design.pipeline.PipelineStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.PipelineStepView;
import com.lombardrisk.ignis.client.design.pipeline.PipelineView;
import com.lombardrisk.ignis.client.design.pipeline.test.StepTestStatus;
import com.lombardrisk.ignis.client.design.pipeline.test.request.CreatePipelineStepDataRowRequest;
import com.lombardrisk.ignis.client.design.pipeline.test.request.CreateStepTestRequest;
import com.lombardrisk.ignis.client.design.pipeline.test.request.RowCellDataRequest;
import com.lombardrisk.ignis.client.design.pipeline.test.request.UpdateStepTestRequest;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepRowOutputDataView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestRowView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.TestRunResultView;
import com.lombardrisk.ignis.client.design.productconfig.NewProductConfigRequest;
import com.lombardrisk.ignis.client.design.productconfig.ProductConfigClient;
import com.lombardrisk.ignis.client.design.productconfig.ProductConfigDto;
import com.lombardrisk.ignis.client.design.productconfig.UpdateProductConfig;
import com.lombardrisk.ignis.client.design.rule.RuleDto;
import com.lombardrisk.ignis.client.design.schema.NewSchemaVersionRequest;
import com.lombardrisk.ignis.client.design.schema.SchemaClient;
import com.lombardrisk.ignis.client.design.schema.SchemaDto;
import com.lombardrisk.ignis.client.design.schema.UpdateSchema;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto.BooleanFieldDto;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto.DateFieldDto;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto.DecimalFieldDto;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto.DoubleFieldDto;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto.FloatFieldDto;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto.IntegerFieldDto;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto.LongFieldDto;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto.StringFieldDto;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto.TimestampFieldDto;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport.BooleanFieldExport;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport.DateFieldExport;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport.DecimalFieldExport;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport.DoubleFieldExport;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport.FloatFieldExport;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport.IntegerFieldExport;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport.LongFieldExport;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport.StringFieldExport;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport.TimestampFieldExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;
import com.lombardrisk.ignis.client.external.productconfig.export.ValidationRuleExport;
import com.lombardrisk.ignis.client.external.productconfig.export.ValidationRuleExport.Severity;
import com.lombardrisk.ignis.client.external.productconfig.export.ValidationRuleExport.Type;
import com.lombardrisk.ignis.functional.test.config.properties.ClientProperties;
import com.lombardrisk.ignis.functional.test.steps.service.PipelineService;
import com.lombardrisk.ignis.functional.test.steps.service.RuleService;
import com.lombardrisk.ignis.functional.test.steps.service.TableService;
import okhttp3.ResponseBody;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.lombardrisk.ignis.functional.test.steps.CallAssertion.callAndExpectSuccess;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

public class DesignStudioSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesignStudioSteps.class);
    private static final int RANDOM_CHARACTER_LENGTH = 10;

    private final ProductConfigClient productConfigClient;
    private final SchemaClient schemaClient;
    private final PipelineClient pipelineClient;
    private final TableService tableService;
    private final RuleService ruleService;
    private final PipelineService pipelineService;
    private final ClientProperties clientProperties;

    public DesignStudioSteps(
            final ProductConfigClient productConfigClient,
            final SchemaClient schemaClient,
            final PipelineClient pipelineClient,
            final TableService tableService,
            final RuleService ruleService,
            final PipelineService pipelineService,
            final ClientProperties clientProperties) {
        this.productConfigClient = productConfigClient;
        this.schemaClient = schemaClient;
        this.pipelineClient = pipelineClient;
        this.tableService = tableService;
        this.ruleService = ruleService;
        this.pipelineService = pipelineService;
        this.clientProperties = clientProperties;
    }

    public SchemaDto createTable(final long productId, final String schemaFileName) throws IOException {
        SchemaDto schemaFromFile = tableService.generateSchemaDtoWithUniqueName(schemaFileName);
        SchemaDto createdSchema = callAndExpectSuccess(schemaClient.createSchema(productId, schemaFromFile));
        LOGGER.info("Created schema with id [{}] name [{}] version [{}]",
                createdSchema.getId(), createdSchema.getPhysicalTableName(), createdSchema.getMajorVersion());

        for (FieldDto field : schemaFromFile.getFields()) {
            FieldDto createdField =
                    callAndExpectSuccess(schemaClient.createField(productId, createdSchema.getId(), field));
            LOGGER.info(
                    "Created field [{}] for schema [{}] name [{}]",
                    createdField.getId(),
                    createdSchema.getId(),
                    field.getName());
        }

        return callAndExpectSuccess(schemaClient.getSchema(productId, createdSchema.getId()));
    }

    public PipelineView createPipeline(final long productId, final String pipelineName) {
        String randomisedName = pipelineName + "_" + randomAlphabetic(RANDOM_CHARACTER_LENGTH);

        PipelineView created =
                callAndExpectSuccess(pipelineClient.createPipeline(CreatePipelineRequest.builder()
                        .productId(productId)
                        .name(randomisedName)
                        .build()));

        LOGGER.info("Created pipeline with id [{}] name [{}] ", created.getId(), randomisedName);

        return callAndExpectSuccess(pipelineClient.findPipeline(created.getId()));
    }

    public PipelineStepView createPipelineMapStep(
            final long pipelineId,
            final PipelineStepRequest pipelineStepRequest) {

        PipelineStepView created =
                callAndExpectSuccess(pipelineClient.createPipelineStep(pipelineStepRequest, pipelineId));

        LOGGER.info("Created pipeline step with id [{}] name [{}] ", created.getId(), created.getName());

        return created;
    }

    public IdView createPipelineStepTest(
            final long pipelineStepId,
            final String testName) {

        IdView created =
                callAndExpectSuccess(pipelineClient.createPipelineStepTest(
                        CreateStepTestRequest.builder()
                                .name(testName)
                                .pipelineStepId(pipelineStepId)
                                .build()));

        LOGGER.info(
                "Created pipeline step test with id [{}] for step [{}] name [{}] ",
                created.getId(), pipelineStepId, testName);

        return created;
    }

    public StepTestView updatePipelineStepTest(
            final long pipelineStepTestId,
            final String testName,
            final String description) {

        StepTestView updated =
                callAndExpectSuccess(pipelineClient.updatePipelineStepTest(
                        pipelineStepTestId,
                        UpdateStepTestRequest.builder()
                                .name(testName)
                                .description(description)
                                .build()));

        LOGGER.info(
                "Updated pipeline step test with id [{}] for name [{}] ",
                updated.getId(), updated.getName());

        return updated;
    }

    public void deletePipelineStepTest(
            final long pipelineStepTestId) {

        callAndExpectSuccess(
                pipelineClient.deletePipelineStepTest(pipelineStepTestId));

        LOGGER.info(
                "Deleted pipeline step test with id [{}] ", pipelineStepTestId);
    }

    public List<Map<Long, String>> readCsvData(
            final SchemaDto schemaDto,
            final String pathToCsvFile) throws IOException {
        return pipelineService.readCsvData(schemaDto, pathToCsvFile);
    }

    private StepTestRowView createPipelineTestInputDataRow(
            final long testId,
            final long schemaId) {

        StepTestRowView created =
                callAndExpectSuccess(pipelineClient.createPipelineTestInputDataRow(
                        testId,
                        CreatePipelineStepDataRowRequest.builder()
                                .schemaId(schemaId)
                                .build()));

        LOGGER.info(
                "Created pipeline step test input data row with id [{}] for test [{}] schema [{}] ",
                created.getId(), testId, schemaId);

        return created;
    }

    public StepTestStatus runTest(final long testId) {

        TestRunResultView created =
                callAndExpectSuccess(pipelineClient.runTest(
                        testId));

        LOGGER.info(
                "Run step test with id [{}]", testId);

        return created.getStatus();
    }

    public void deletePipelineTestExpectedDataRow(final long testId, final long rowId) {

        callAndExpectSuccess(pipelineClient.deleteExpectedDataRow(testId, rowId));

        LOGGER.info(
                "Deleted piple step test expected data test id [{}] row id [{}]", testId, rowId);
    }

    private StepTestRowView createPipelineTestExpectedDataRow(
            final long testId,
            final long schemaId) {

        StepTestRowView created =
                callAndExpectSuccess(pipelineClient.createPipelineTestExpectedDataRow(
                        testId,
                        CreatePipelineStepDataRowRequest.builder()
                                .schemaId(schemaId)
                                .build()));

        LOGGER.info(
                "Created pipeline step test expected data row with id [{}] for test [{}] schema [{}] ",
                created.getId(), testId, schemaId);

        return created;
    }

    public StepTestView createPipelineTestExpectedDataRow(
            final long testId) {

        StepTestView retrieved =
                callAndExpectSuccess(pipelineClient.getPipelineStepTest(
                        testId));

        LOGGER.info(
                "Retrieved pipeline step test with id [{}]", testId);

        return retrieved;
    }

    public List<StepRowOutputDataView> getPipelineStepTestOutputRows(final long testId) {
        List<StepRowOutputDataView> outputRows =
                callAndExpectSuccess(pipelineClient.getTestOutputRows(testId));

        LOGGER.info("Retrieved pipeline step test output rows with id [{}]", testId);

        return outputRows;
    }

    private void updateCellValue(
            final long testId,
            final long rowId,
            final long cellId,
            final String data) {

        StepTestRowView created = callAndExpectSuccess(pipelineClient.patchRowCellData(testId, rowId, cellId,
                RowCellDataRequest.builder()
                        .inputDataValue(data)
                        .build()
        ));

        LOGGER.info(
                "Updated pipeline step test row cell with id [{}] for row [{}] cell [{}] data [{}]",
                created.getId(), rowId, cellId, data);
    }

    public void createInputDataRowAndUpdateCells(
            final long schemaId,
            final long testId,
            final List<Map<Long, String>> inputRow) {
        for (Map<Long, String> row : inputRow) {
            StepTestRowView createdInputRow = createPipelineTestInputDataRow(testId, schemaId);
            for (StepTestRowView.CellIdAndField cell : createdInputRow.getCells()) {
                String data = row.get(cell.getFieldId());
                updateCellValue(testId, createdInputRow.getId(), cell.getId(), data);
            }
        }
    }

    public List<Long> createExpectedDataRowAndUpdateCells(
            final long schemaId,
            final long testId,
            final List<Map<Long, String>> expectedFailData) {
        List<Long> expectedDataRowIds = new ArrayList<>();
        for (Map<Long, String> row : expectedFailData) {
            StepTestRowView createdExpectedRow = createPipelineTestExpectedDataRow(testId, schemaId);
            for (StepTestRowView.CellIdAndField cell : createdExpectedRow.getCells()) {
                String data = row.get(cell.getFieldId());
                updateCellValue(testId, createdExpectedRow.getId(), cell.getId(), data);
            }
            expectedDataRowIds.add(createdExpectedRow.getId());
        }
        return expectedDataRowIds;
    }

    public void addRules(
            final String ruleSetFileName,
            final Long productId,
            final SchemaDto table)
            throws IOException {

        List<ValidationRuleExport> newRules = ruleService.saveRules(ruleSetFileName, productId, table.getId());

        newRules.forEach(rule -> LOGGER.info("Created rule with id [{}] name [{}] for table id [{}]",
                rule.getId(), rule.getName(), table.getId()));
    }

    public List<FieldDto> addFields(
            final Long productId,
            final Long schemaId,
            final Iterable<FieldDto> fields) {

        List<FieldDto> createdFields = new ArrayList<>();
        for (FieldDto field : fields) {
            FieldDto createdField =
                    callAndExpectSuccess(schemaClient.createField(productId, schemaId, field));
            LOGGER.info(
                    "Created field [{}] for schema [{}] name [{}]",
                    createdField.getId(),
                    schemaId,
                    field.getName());
        }

        return createdFields;
    }

    public ProductConfigDto createProductConfig(final NewProductConfigRequest productConfigRequest) {
        ProductConfigDto savedProductConfig =
                callAndExpectSuccess(productConfigClient.saveProductConfig(productConfigRequest));

        LOGGER.info("Created product config with id [{}] name [{}]",
                savedProductConfig.getId(), savedProductConfig.getName());

        return savedProductConfig;
    }

    public ProductConfigDto findProduct(final Long productId) {
        List<ProductConfigDto> productConfigViews = callAndExpectSuccess(productConfigClient.findAllProductConfigs());

        return productConfigViews.stream()
                .filter(productConfigDto -> productConfigDto.getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Cannot find design product with id " + productId));
    }

    private static List<SchemaExport> toSchemaViews(final List<SchemaDto> schemas) {
        return schemas.stream()
                .map(schemaDto -> SchemaExport.builder()
                        .id(schemaDto.getId())
                        .displayName(schemaDto.getDisplayName())
                        .physicalTableName(schemaDto.getPhysicalTableName())
                        .version(schemaDto.getMajorVersion())
                        .startDate(fromLocalDate(schemaDto.getStartDate()))
                        .endDate(Optional.ofNullable(schemaDto.getEndDate())
                                .map(DesignStudioSteps::fromLocalDate)
                                .orElse(null))
                        .createdBy(schemaDto.getCreatedBy())
                        .createdTime(schemaDto.getCreatedTime())
                        .fields(toFieldViews(schemaDto.getFields()))
                        .validationRules(toValidationRules(schemaDto.getValidationRules()))
                        .build())
                .collect(toList());
    }

    private static Date fromLocalDate(final LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().toInstant(ZoneOffset.UTC));
    }

    private static List<FieldExport> toFieldViews(final List<FieldDto> schemaDtoFields) {
        return schemaDtoFields.stream()
                .map(DesignStudioSteps::toFieldView)
                .collect(toList());
    }

    private static FieldExport toFieldView(final FieldDto fieldDto) {
        if (fieldDto instanceof StringFieldDto) {
            return toStringFieldView((StringFieldDto) fieldDto);
        }
        if (fieldDto instanceof DecimalFieldDto) {
            return toDecimalFieldView((DecimalFieldDto) fieldDto);
        }
        if (fieldDto instanceof LongFieldDto) {
            return toLongFieldView((LongFieldDto) fieldDto);
        }
        if (fieldDto instanceof IntegerFieldDto) {
            return toIntegerFieldView((IntegerFieldDto) fieldDto);
        }
        if (fieldDto instanceof DoubleFieldDto) {
            return toDoubleFieldView((DoubleFieldDto) fieldDto);
        }
        if (fieldDto instanceof FloatFieldDto) {
            return toFloatFieldView((FloatFieldDto) fieldDto);
        }
        if (fieldDto instanceof DateFieldDto) {
            return toDateFieldView((DateFieldDto) fieldDto);
        }
        if (fieldDto instanceof TimestampFieldDto) {
            return toTimestampFieldView((TimestampFieldDto) fieldDto);
        }
        if (fieldDto instanceof BooleanFieldDto) {
            return toBooleanFieldView((BooleanFieldDto) fieldDto);
        }
        throw new AssertionError("Cannot map field " + fieldDto);
    }

    private static StringFieldExport toStringFieldView(final StringFieldDto stringFieldDto) {
        return StringFieldExport.builder()
                .id(stringFieldDto.getId())
                .name(stringFieldDto.getName())
                .nullable(stringFieldDto.isNullable())
                .maxLength(stringFieldDto.getMaxLength())
                .minLength(stringFieldDto.getMinLength())
                .regularExpression(stringFieldDto.getRegularExpression())
                .build();
    }

    private static DecimalFieldExport toDecimalFieldView(final DecimalFieldDto decimalFieldDto) {
        return DecimalFieldExport.builder()
                .id(decimalFieldDto.getId())
                .name(decimalFieldDto.getName())
                .nullable(decimalFieldDto.isNullable())
                .precision(decimalFieldDto.getPrecision())
                .scale(decimalFieldDto.getScale())
                .build();
    }

    private static LongFieldExport toLongFieldView(final LongFieldDto longFieldDto) {
        return LongFieldExport.builder()
                .id(longFieldDto.getId())
                .name(longFieldDto.getName())
                .nullable(longFieldDto.isNullable())
                .build();
    }

    private static IntegerFieldExport toIntegerFieldView(final IntegerFieldDto integerFieldDto) {
        return IntegerFieldExport.builder()
                .id(integerFieldDto.getId())
                .name(integerFieldDto.getName())
                .nullable(integerFieldDto.isNullable())
                .build();
    }

    private static DoubleFieldExport toDoubleFieldView(final DoubleFieldDto doubleFieldDto) {
        return DoubleFieldExport.builder()
                .id(doubleFieldDto.getId())
                .name(doubleFieldDto.getName())
                .nullable(doubleFieldDto.isNullable())
                .build();
    }

    private static FloatFieldExport toFloatFieldView(final FloatFieldDto floatFieldDto) {
        return FloatFieldExport.builder()
                .id(floatFieldDto.getId())
                .name(floatFieldDto.getName())
                .nullable(floatFieldDto.isNullable())
                .build();
    }

    private static DateFieldExport toDateFieldView(final DateFieldDto dateFieldDto) {
        return DateFieldExport.builder()
                .id(dateFieldDto.getId())
                .name(dateFieldDto.getName())
                .nullable(dateFieldDto.isNullable())
                .format(dateFieldDto.getFormat())
                .build();
    }

    private static TimestampFieldExport toTimestampFieldView(final TimestampFieldDto timestampFieldDto) {
        return TimestampFieldExport.builder()
                .id(timestampFieldDto.getId())
                .name(timestampFieldDto.getName())
                .nullable(timestampFieldDto.isNullable())
                .format(timestampFieldDto.getFormat())
                .build();
    }

    private static BooleanFieldExport toBooleanFieldView(final BooleanFieldDto booleanFieldDto) {
        return BooleanFieldExport.builder()
                .id(booleanFieldDto.getId())
                .name(booleanFieldDto.getName())
                .nullable(booleanFieldDto.isNullable())
                .build();
    }

    private static List<ValidationRuleExport> toValidationRules(final List<RuleDto> validationRules) {
        return validationRules.stream()
                .map(rule -> ValidationRuleExport.builder()
                        .id(rule.getId())
                        .ruleId(rule.getRuleId())
                        .name(rule.getName())
                        .version(rule.getVersion())
                        .validationRuleType(Type.valueOf(rule.getValidationRuleType().name()))
                        .validationRuleSeverity(Severity.valueOf(rule.getValidationRuleSeverity().name()))
                        .description(rule.getDescription())
                        .startDate(rule.getStartDate())
                        .endDate(rule.getEndDate())
                        .expression(rule.getExpression())
                        .build())
                .collect(toList());
    }

    public void deleteProductConfig(final long id) {
        callAndExpectSuccess(productConfigClient.deleteProductConfig(id));

        LOGGER.info("Removed product config from design studio with id [{}]", id);
    }

    public File exportProductConfig(final long id, final String name) throws IOException {
        ResponseBody response = callAndExpectSuccess(productConfigClient.exportProductConfig(id));

        String fileName = name + ".zip";
        File file = clientProperties.getDownloadsPath().resolve(fileName).toFile();

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            IOUtils.copy(response.byteStream(), fileOutputStream);

            LOGGER.info("Exported product config to [{}]", file.getAbsolutePath());
        }

        return file;
    }

    public ProductConfigDto updateProduct(final ProductConfigDto productConfigUpdateRequest) {
        Call<ProductConfigDto> updateProductCall = productConfigClient.updateProduct(
                productConfigUpdateRequest.getId(),
                UpdateProductConfig.builder()
                        .name(productConfigUpdateRequest.getName())
                        .version(productConfigUpdateRequest.getVersion())
                        .build());
        return callAndExpectSuccess(updateProductCall);
    }

    public SchemaDto createNextVersionedSchema(
            final Long designProductId,
            final Long schemaId,
            final LocalDate startDate) {
        return callAndExpectSuccess(
                productConfigClient.createNextVersion(
                        designProductId,
                        schemaId,
                        NewSchemaVersionRequest.builder()
                                .startDate(startDate)
                                .build()));
    }

    public SchemaDto addSchema(final Long designProductId, final SchemaDto newSchemaView) {
        return callAndExpectSuccess(
                productConfigClient.addSchema(designProductId, newSchemaView));
    }

    public SchemaDto getSchema(final Long designProductId, final Long schemaId) {
        return callAndExpectSuccess(
                schemaClient.getSchema(designProductId, schemaId));
    }

    public void editSchema(final Long productId, final Long schemaId, final UpdateSchema updatedSchemaRequest) {
        callAndExpectSuccess(
                productConfigClient.editSchema(productId, schemaId, updatedSchemaRequest));
    }

    private SchemaDto toSchemaDto(final SchemaExport updatedSchemaView) {
        return SchemaDto.builder()
                .physicalTableName(updatedSchemaView.getPhysicalTableName())
                .displayName(updatedSchemaView.getDisplayName())
                .majorVersion(updatedSchemaView.getVersion())
                .createdBy(updatedSchemaView.getCreatedBy())
                .createdTime(updatedSchemaView.getCreatedTime())
                .startDate(toLocalDate(updatedSchemaView.getStartDate()))

                .endDate(Optional.ofNullable(updatedSchemaView.getEndDate())
                        .map(this::toLocalDate)
                        .orElse(null))
                .fields(toFieldDtos(updatedSchemaView.getFields()))
                .validationRules(toRuleDtos(updatedSchemaView.getValidationRules()))
                .build();
    }

    private LocalDate toLocalDate(final Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalDate();
    }

    private static List<RuleDto> toRuleDtos(final List<ValidationRuleExport> validationRules) {
        if (isEmpty(validationRules)) {
            return emptyList();
        }
        return validationRules.stream()
                .map(ruleExport -> RuleDto.builder()
                        .id(ruleExport.getId())
                        .ruleId(ruleExport.getRuleId())
                        .name(ruleExport.getName())
                        .version(ruleExport.getVersion())
                        .validationRuleSeverity(RuleDto.Severity.valueOf(ruleExport.getValidationRuleSeverity().name()))
                        .validationRuleType(RuleDto.Type.valueOf(ruleExport.getValidationRuleType().name()))
                        .startDate(ruleExport.getStartDate())
                        .endDate(ruleExport.getEndDate())
                        .description(ruleExport.getDescription())
                        .expression(ruleExport.getExpression())
                        .build())
                .collect(toList());
    }

    private static List<FieldDto> toFieldDtos(final List<FieldExport> fields) {
        return fields.stream()
                .map(DesignStudioSteps::toFieldDto)
                .collect(toList());
    }

    private static FieldDto toFieldDto(final FieldExport fieldView) {
        if (fieldView instanceof StringFieldExport) {
            return toStringFieldDto((StringFieldExport) fieldView);
        }
        if (fieldView instanceof DecimalFieldExport) {
            return toDecimalFieldDto((DecimalFieldExport) fieldView);
        }
        if (fieldView instanceof LongFieldExport) {
            return toLongFieldDto((LongFieldExport) fieldView);
        }
        if (fieldView instanceof IntegerFieldExport) {
            return toIntegerFieldDto((IntegerFieldExport) fieldView);
        }
        if (fieldView instanceof DoubleFieldExport) {
            return toDoubleFieldDto((DoubleFieldExport) fieldView);
        }
        if (fieldView instanceof FloatFieldExport) {
            return toFloatFieldDto((FloatFieldExport) fieldView);
        }
        if (fieldView instanceof DateFieldExport) {
            return toDateFieldDto((DateFieldExport) fieldView);
        }
        if (fieldView instanceof TimestampFieldExport) {
            return toTimestampFieldDto((TimestampFieldExport) fieldView);
        }
        if (fieldView instanceof BooleanFieldExport) {
            return toBooleanFieldDto((BooleanFieldExport) fieldView);
        }
        throw new AssertionError("Cannot map field " + fieldView);
    }

    private static StringFieldDto toStringFieldDto(final StringFieldExport stringFieldView) {
        return StringFieldDto.builder()
                .id(stringFieldView.getId())
                .name(stringFieldView.getName())
                .nullable(stringFieldView.isNullable())
                .maxLength(stringFieldView.getMaxLength())
                .minLength(stringFieldView.getMinLength())
                .regularExpression(stringFieldView.getRegularExpression())
                .build();
    }

    private static DecimalFieldDto toDecimalFieldDto(final DecimalFieldExport decimalFieldView) {
        return DecimalFieldDto.builder()
                .id(decimalFieldView.getId())
                .name(decimalFieldView.getName())
                .nullable(decimalFieldView.isNullable())
                .precision(decimalFieldView.getPrecision())
                .scale(decimalFieldView.getScale())
                .build();
    }

    private static LongFieldDto toLongFieldDto(final LongFieldExport longFieldView) {
        return LongFieldDto.builder()
                .id(longFieldView.getId())
                .name(longFieldView.getName())
                .nullable(longFieldView.isNullable())
                .build();
    }

    private static IntegerFieldDto toIntegerFieldDto(final IntegerFieldExport integerFieldView) {
        return IntegerFieldDto.builder()
                .id(integerFieldView.getId())
                .name(integerFieldView.getName())
                .nullable(integerFieldView.isNullable())
                .build();
    }

    private static DoubleFieldDto toDoubleFieldDto(final DoubleFieldExport doubleFieldView) {
        return DoubleFieldDto.builder()
                .id(doubleFieldView.getId())
                .name(doubleFieldView.getName())
                .nullable(doubleFieldView.isNullable())
                .build();
    }

    private static FloatFieldDto toFloatFieldDto(final FloatFieldExport floatFieldView) {
        return FloatFieldDto.builder()
                .id(floatFieldView.getId())
                .name(floatFieldView.getName())
                .nullable(floatFieldView.isNullable())
                .build();
    }

    private static DateFieldDto toDateFieldDto(final DateFieldExport dateFieldView) {
        return DateFieldDto.builder()
                .id(dateFieldView.getId())
                .name(dateFieldView.getName())
                .nullable(dateFieldView.isNullable())
                .format(dateFieldView.getFormat())
                .build();
    }

    private static TimestampFieldDto toTimestampFieldDto(final TimestampFieldExport timestampFieldView) {
        return TimestampFieldDto.builder()
                .id(timestampFieldView.getId())
                .name(timestampFieldView.getName())
                .nullable(timestampFieldView.isNullable())
                .format(timestampFieldView.getFormat())
                .build();
    }

    private static BooleanFieldDto toBooleanFieldDto(final BooleanFieldExport booleanFieldView) {
        return BooleanFieldDto.builder()
                .id(booleanFieldView.getId())
                .name(booleanFieldView.getName())
                .nullable(booleanFieldView.isNullable())
                .build();
    }
}
