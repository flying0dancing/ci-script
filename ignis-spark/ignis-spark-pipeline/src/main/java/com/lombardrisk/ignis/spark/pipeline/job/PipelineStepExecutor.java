package com.lombardrisk.ignis.spark.pipeline.job;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.common.stream.CollectorUtils;
import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import com.lombardrisk.ignis.pipeline.step.api.UnionAppConfig;
import com.lombardrisk.ignis.pipeline.step.api.UnionSpec;
import com.lombardrisk.ignis.pipeline.step.common.MapStepExecutor;
import com.lombardrisk.ignis.pipeline.step.common.UnionStepExecutor;
import com.lombardrisk.ignis.pipeline.step.common.WindowStepExecutor;
import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.api.pipeline.AggregateStepAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.JoinStepAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.MapStepAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineStepAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineStepDatasetLookup;
import com.lombardrisk.ignis.spark.api.pipeline.ScriptletStepAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.UnionStepAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.WindowStepAppConfig;
import com.lombardrisk.ignis.spark.core.repository.DatasetRepository;
import com.lombardrisk.ignis.spark.core.repository.RowKeyedDatasetRepository;
import com.lombardrisk.ignis.spark.pipeline.job.step.AggregationStepExecutor;
import com.lombardrisk.ignis.spark.pipeline.job.step.JoinStepExecutor;
import com.lombardrisk.ignis.spark.script.api.Scriptlet;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Slf4j
@AllArgsConstructor
public class PipelineStepExecutor {

    private final RowKeyedDatasetRepository rowKeyedDatasetRepository;
    private final DatasetRepository datasetRepository;
    private final AggregationStepExecutor aggregationStepExecutor;
    private final MapStepExecutor mapStepExecutor;
    private final JoinStepExecutor joinStepExecutor;
    private final WindowStepExecutor windowStepExecutor;
    private final UnionStepExecutor unionStepExecutor;

    public DatasetTableLookup runPipelineStep(
            final PipelineAppConfig pipelineAppConfig,
            final PipelineStepAppConfig pipelineStepAppConfig,
            final Map<Long, DatasetTableLookup> outputDatasets) {

        List<DatasetTableLookup> datasetInputs =
                validateInputs(pipelineAppConfig, pipelineStepAppConfig, outputDatasets);

        String outputPhysicalTableName = pipelineStepAppConfig.getOutputDataset()
                .getStagingSchemaValidation()
                .getPhysicalTableName();

        if (datasetInputs.stream()
                .map(DatasetTableLookup::getDatasetName)
                .anyMatch(name -> name.equals(outputPhysicalTableName))) {
            throw new IllegalArgumentException("Cannot save to same table as input, data will be overridden");
        }

        Dataset<Row> outputDataset = runTransformation(pipelineStepAppConfig, datasetInputs);

        return saveOutputDataset(
                pipelineAppConfig,
                pipelineStepAppConfig,
                outputPhysicalTableName,
                outputDataset);
    }

    private Dataset<Row> runTransformation(
            final PipelineStepAppConfig pipelineStepAppConfig,
            final List<DatasetTableLookup> datasetInputs) {

        switch (pipelineStepAppConfig.getTransformationType()) {
            case AGGREGATION:
                return aggregationStepExecutor.runAggregation(
                        (AggregateStepAppConfig) pipelineStepAppConfig,
                        datasetInputs.get(0));
            case MAP:
                return runMapStep((MapStepAppConfig) pipelineStepAppConfig, datasetInputs);
            case WINDOW:
                return runWindowStep((WindowStepAppConfig) pipelineStepAppConfig, datasetInputs);
            case JOIN:
                return joinStepExecutor.runJoin((JoinStepAppConfig) pipelineStepAppConfig, datasetInputs);
            case UNION:
                return runUnionStep((UnionStepAppConfig) pipelineStepAppConfig, datasetInputs);
            case SCRIPTLET:
                return runScriptletStep((ScriptletStepAppConfig) pipelineStepAppConfig, datasetInputs);
            default:
                throw new IllegalArgumentException("Transformation Type not supported: "
                        + pipelineStepAppConfig.getTransformationType());
        }
    }

    private Dataset<Row> runUnionStep(
            final UnionStepAppConfig unionStepAppConfig,
            final List<DatasetTableLookup> datasetInputs) {

        Map<UnionSpec, Set<SelectColumn>> selectMap = unionStepAppConfig.getUnions().stream()
                .collect(Collectors.toMap(UnionAppConfig::getSchemaIn, UnionAppConfig::getSelects));

        Map<UnionSpec, Set<String>> filterMap = unionStepAppConfig.getUnions().stream()
                .collect(Collectors.toMap(
                        UnionAppConfig::getSchemaIn,
                        union -> union.getFilters() == null ? Collections.emptySet() : union.getFilters()));

        Map<String, Dataset<Row>> datasets = new HashMap<>();
        for (DatasetTableLookup datasetInput : datasetInputs) {
            Dataset<Row> rowDataset = datasetRepository.readDataFrame(datasetInput);
            datasets.put(datasetInput.getDatasetName(), rowDataset);
        }

        return unionStepExecutor.runUnion(selectMap, filterMap, datasets);
    }

    private Dataset<Row> runMapStep(
            final MapStepAppConfig pipelineStepAppConfig,
            final List<DatasetTableLookup> datasetInputs) {
        Dataset<Row> inputDataset = datasetRepository.readDataFrame(datasetInputs.get(0));
        return mapStepExecutor.runMap(
                pipelineStepAppConfig.getSelects(),
                pipelineStepAppConfig.getFilters(),
                datasetInputs.get(0).getDatasetName(),
                inputDataset);
    }

    private Dataset<Row> runScriptletStep(
            final ScriptletStepAppConfig pipelineStepAppConfig,
            final List<DatasetTableLookup> datasetInputs) {

        try {
            Class<?> scriptletClass = ClassLoader.getSystemClassLoader()
                    .loadClass(pipelineStepAppConfig.getClassName());

            if (!Scriptlet.class.isAssignableFrom(scriptletClass)) {
                log.error("{} is not a {}", scriptletClass, Scriptlet.class.getSimpleName());
                throw new IllegalArgumentException(
                        String.format("%s is not a %s", scriptletClass.getName(), Scriptlet.class.getSimpleName()));
            }

            @SuppressWarnings("unchecked")
            Scriptlet scriptlet = ((Class<? extends Scriptlet>) scriptletClass).newInstance();

            Validation<List<String>, Map<String, DatasetTableLookup>> stepInputValidation =
                    validateScriptletInputs(scriptlet, pipelineStepAppConfig, datasetInputs);

            if (stepInputValidation.isInvalid()) {
                String missingScriptletInputs = String.join(", ", stepInputValidation.getError());
                throw new IllegalArgumentException(
                        String.format("Required input trait of %s not found: [%s]",
                                scriptletClass.getName(), missingScriptletInputs));
            }

            Map<String, DatasetTableLookup> inputTraitsToDatasetLookups = stepInputValidation.get();
            Map<String, Dataset<Row>> scriptletInputsToDatasets = new HashMap<>();
            for (Map.Entry<String, DatasetTableLookup> datasetLookup : inputTraitsToDatasetLookups.entrySet()) {
                Dataset<Row> dataset = datasetRepository.readDataFrame(datasetLookup.getValue());
                scriptletInputsToDatasets.put(datasetLookup.getKey(), dataset);
            }

            return scriptlet.run(scriptletInputsToDatasets);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            log.error("Could not load scriptlet from class [{}]", pipelineStepAppConfig.getClassName());
            throw new IllegalStateException("Could not run scriptlet step", e);
        }
    }

    private Validation<List<String>, Map<String, DatasetTableLookup>> validateScriptletInputs(
            final Scriptlet scriptlet,
            final ScriptletStepAppConfig scriptletConfig,
            final List<DatasetTableLookup> datasetInputs) {

        Set<String> scriptletInputTraitNames = scriptlet.inputTraits().keySet();
        Map<String, String> inputTraitsToSchemas = scriptletConfig.getInputSchemaMappings();

        Sets.SetView<String> missingTraits = Sets.difference(scriptletInputTraitNames, inputTraitsToSchemas.keySet());
        if (!missingTraits.isEmpty()) {
            return Validation.invalid(ImmutableList.copyOf(missingTraits).stream()
                    .sorted()
                    .collect(toList()));
        }

        Map<String, DatasetTableLookup> scriptletInputs = new HashMap<>();
        List<String> missingDatasets = new ArrayList<>();
        for (String inputTrait : scriptletInputTraitNames) {
            String schemaName = inputTraitsToSchemas.get(inputTrait);

            Optional<DatasetTableLookup> datasetLookup = datasetInputs.stream()
                    .filter(input -> input.getDatasetName().equals(schemaName))
                    .findFirst();

            if (datasetLookup.isPresent()) {
                scriptletInputs.put(inputTrait, datasetLookup.get());
            } else {
                missingDatasets.add(inputTrait);
            }
        }

        if (!missingDatasets.isEmpty()) {
            return Validation.invalid(missingDatasets);
        }

        return Validation.valid(scriptletInputs);
    }

    private Dataset<Row> runWindowStep(
            final WindowStepAppConfig pipelineStepAppConfig,
            final List<DatasetTableLookup> datasetInputs) {
        Dataset<Row> inputDataset = datasetRepository.readDataFrame(datasetInputs.get(0));
        return windowStepExecutor.runWindow(
                pipelineStepAppConfig.getSelects(),
                pipelineStepAppConfig.getFilters(),
                datasetInputs.get(0).getDatasetName(),
                inputDataset);
    }

    private List<DatasetTableLookup> validateInputs(
            final PipelineAppConfig pipelineAppConfig,
            final PipelineStepAppConfig pipelineStepAppConfig, final Map<Long, DatasetTableLookup> outputDatasets) {
        log.debug("Running pipeline job for item {}, step {}",
                pipelineAppConfig.getPipelineInvocationId(), pipelineStepAppConfig);

        Validation<List<String>, List<DatasetTableLookup>> datasetInputsValidation =
                findRequiredDatasetInputs(pipelineStepAppConfig, outputDatasets);

        if (datasetInputsValidation.isInvalid()) {
            throw new IllegalStateException("Cannot find all datasets for pipeline step ["
                    + pipelineStepAppConfig.getPipelineStepInvocationId()
                    + "]");
        }

        return datasetInputsValidation.get();
    }

    private DatasetTableLookup saveOutputDataset(
            final PipelineAppConfig pipelineAppConfig,
            final PipelineStepAppConfig pipelineStepAppConfig,
            final String outputPhysicalTableName, final Dataset<Row> outputDataset) {

        log.debug("Creating output dataset {}", outputPhysicalTableName);

        long rowKeySeed = pipelineAppConfig.getServiceRequestId();

        return rowKeyedDatasetRepository.generateRowKeyAndSaveDataset(
                rowKeySeed, outputDataset, pipelineAppConfig, pipelineStepAppConfig);
    }

    private Validation<List<String>, List<DatasetTableLookup>> findRequiredDatasetInputs(
            final PipelineStepAppConfig pipelineStepAppConfig,
            final Map<Long, DatasetTableLookup> outputDatasets) {

        return pipelineStepAppConfig.getPipelineStepDatasetLookups().stream()
                .map(pipelineStepDatasetLookup ->
                        getDatasetTableLookup(pipelineStepDatasetLookup, outputDatasets))
                .collect(CollectorUtils.groupValidations());
    }

    private static boolean isOneToOneTransformation(final PipelineStepAppConfig config) {
        return config.getTransformationType() == TransformationType.MAP
                || config.getTransformationType() == TransformationType.WINDOW;
    }

    private Validation<String, DatasetTableLookup> getDatasetTableLookup(
            final PipelineStepDatasetLookup pipelineStepDatasetLookup,
            final Map<Long, DatasetTableLookup> outputDatasets) {

        Either<DatasetTableLookup, Long> lookup = pipelineStepDatasetLookup.toLookup();

        if (lookup.isRight()) {
            Long pipelineStepInvocationId = lookup.get();
            return Option.of(outputDatasets.get(pipelineStepInvocationId))
                    .toValid("Dataset lookup for pipeline step invocation ["
                            + pipelineStepInvocationId
                            + "] not found");
        }

        return Validation.valid(lookup.getLeft());
    }
}
