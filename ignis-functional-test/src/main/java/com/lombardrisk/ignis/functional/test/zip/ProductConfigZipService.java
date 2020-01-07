package com.lombardrisk.ignis.functional.test.zip;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.common.ZipUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Preconditions.checkState;

public class ProductConfigZipService {

    private static final int RANDOMISED_CHARACTERS = 10;
    private static final String JSON_FILE_TYPE = "json";
    private static final String ZIP_FILE_TYPE = "zip";
    private static final String MANIFEST_NAME = "manifest";
    private static final String MANIFEST_FILENAME = MANIFEST_NAME + "." + JSON_FILE_TYPE;

    private static final JsonFactory JSON_FACTORY = MAPPER.getFactory();
    private static final String PHYSICAL_TABLE_NAME = "physicalTableName";
    private static final String DISPLAY_NAME = "displayName";
    private static final String SCHEMA_OUT = "schemaOut";
    private static final String SCHEMA_IN = "schemaIn";
    private static final int TWO = 2;

    public File writeProductConfig(final File folder) throws IOException {

        Map<String, byte[]> files = readProductConfig(folder);
        Path tempLocation = Files.createTempFile(folder.getName(), ZIP_FILE_TYPE);
        File productZip = tempLocation.toFile();

        try (FileOutputStream outputStream = new FileOutputStream(productZip)) {
            ZipUtils.writeFiles(files, outputStream);
        }

        return productZip;
    }

    private Map<String, byte[]> readProductConfig(final File folder) throws IOException {
        checkState(folder.isDirectory(), "Zip folder location must be a folder");
        String randomisedProductIdentifier = randomAlphanumeric(RANDOMISED_CHARACTERS).toUpperCase();

        try (Stream<Path> directoryContents = Files.walk(Paths.get(folder.getPath()))) {
            Map<Path, byte[]> files = directoryContents
                    .filter(path -> path.toFile().isFile())
                    .collect(Collectors.toMap(path -> path, this::readBytes));

            byte[] manifestFile = files.get(Paths.get(folder.getPath(), MANIFEST_FILENAME));
            if (manifestFile == null) {
                throw new IllegalStateException("No manifest present in " + folder);
            }

            Map<String, byte[]> fileNameToContent = new HashMap<>();
            fileNameToContent.put(MANIFEST_FILENAME, randomiseManifest(manifestFile, randomisedProductIdentifier));

            fileNameToContent.putAll(
                    readTablesAndPipelines(randomisedProductIdentifier, files));

            return ImmutableMap.copyOf(fileNameToContent);
        }
    }

    private Map<String, byte[]> readTablesAndPipelines(
            final String randomisedProductIdentifier, final Map<Path, byte[]> files) throws IOException {

        Map<String, byte[]> fileNameToContent = new HashMap<>();

        for (Map.Entry<Path, byte[]> entry : files.entrySet()) {
            Path path = entry.getKey();
            String fileName = entry.getKey().getFileName().toString();
            byte[] fileContent = entry.getValue();

            if (fileName.equals(MANIFEST_FILENAME)) {
                continue;
            }

            if (path.toString()
                    .contains(Paths.get("pipelines/").toString())) {

                byte[] randomisedPipeline = randomisePipeline(fileContent, randomisedProductIdentifier);
                fileNameToContent.put("pipelines/" + fileName, randomisedPipeline);
                continue;
            }

            byte[] randomisedTable = randomiseTable(fileContent, randomisedProductIdentifier);
            fileNameToContent.put(fileName, randomisedTable);
        }

        return fileNameToContent;
    }

    private byte[] randomiseManifest(
            final byte[] manifest, final String randomisedProductIdentifier) throws IOException {
        JsonParser parser = JSON_FACTORY.createParser(manifest);
        JsonNode manifestObject = MAPPER.readTree(parser);
        String randomisedName = manifestObject.get("name").asText() + randomisedProductIdentifier;

        ObjectNode newManifest = (ObjectNode) manifestObject;
        newManifest.put("name", randomisedName);

        return MAPPER.writeValueAsBytes(newManifest);
    }

    private byte[] randomiseTable(final byte[] table, final String randomisedProductIdentifier) throws IOException {
        JsonParser parser = JSON_FACTORY.createParser(table);
        JsonNode manifestObject = MAPPER.readTree(parser);

        ObjectNode newTable = (ObjectNode) manifestObject;
        randomiseSchemaReference(newTable, randomisedProductIdentifier);

        return MAPPER.writeValueAsBytes(newTable);
    }

    private byte[] randomisePipeline(final byte[] table, final String randomisedProductIdentifier) throws IOException {
        JsonParser parser = JSON_FACTORY.createParser(table);
        ObjectNode node = MAPPER.readTree(parser);

        String randomisedName = node.get("name").asText() + randomisedProductIdentifier;
        node.put("name", randomisedName);

        for (JsonNode jsonNode : node.get("steps")) {
            randomisePipelineStep(randomisedProductIdentifier, jsonNode);
        }

        return MAPPER.writeValueAsBytes(node);
    }

    private void randomisePipelineStep(
            final String randomisedProductIdentifier, final JsonNode node) {

        TransformationType transformationType = TransformationType.valueOf(node.get("type").textValue());

        if (transformationType.equals(TransformationType.JOIN)) {
            randomisePipelineJoinStep(randomisedProductIdentifier, node);
        } else if (transformationType.equals(TransformationType.UNION)) {
            randomisePipelineUnionStep(randomisedProductIdentifier, node);
        } else if (transformationType.equals(TransformationType.SCRIPTLET)) {
            randomisePipelineScriptletStep(randomisedProductIdentifier, node);
        } else {
            randomisePipelineMapOrAggregationStep(randomisedProductIdentifier, node);
        }
    }

    private void randomisePipelineMapOrAggregationStep(
            final String randomisedProductIdentifier, final JsonNode node) {
        ObjectNode schemaIn = (ObjectNode) node.get(SCHEMA_IN);
        ObjectNode schemaOut = (ObjectNode) node.get(SCHEMA_OUT);

        ObjectNode updatedStep = (ObjectNode) node;

        updatedStep.set(SCHEMA_IN, randomiseSchemaReference(schemaIn, randomisedProductIdentifier));
        updatedStep.set(SCHEMA_OUT, randomiseSchemaReference(schemaOut, randomisedProductIdentifier));
    }

    private void randomisePipelineJoinStep(
            final String randomisedProductIdentifier, final JsonNode node) {

        ObjectNode schemaOut = (ObjectNode) node.get(SCHEMA_OUT);

        for (JsonNode join : node.get("joins")) {
            ObjectNode joinNode = (ObjectNode) join;
            ObjectNode leftSchema = (ObjectNode) join.get("left");
            ObjectNode rightSchema = (ObjectNode) join.get("right");
            joinNode.set("left", randomiseSchemaReference(leftSchema, randomisedProductIdentifier));
            joinNode.set("right", randomiseSchemaReference(rightSchema, randomisedProductIdentifier));
        }

        ObjectNode updatedStep = (ObjectNode) node;
        updatedStep.set(SCHEMA_OUT, randomiseSchemaReference(schemaOut, randomisedProductIdentifier));

        randomisePipelineJoinSelects(randomisedProductIdentifier, node);
    }

    /**
     * Requires that all selects be of the format  TABLE_NAME.COLUMN_ID AS OUTPUT_COLUMN
     *
     * @param randomisedProductIdentifier Random product string to ensure test isolation
     * @param joinStepParentNode          Pipeline step to be randomise
     */
    private void randomisePipelineJoinSelects(
            final String randomisedProductIdentifier, final JsonNode joinStepParentNode) {

        for (JsonNode select : joinStepParentNode.get("selects")) {
            ObjectNode selectObjectNode = (ObjectNode) select;
            String previousSelectValue = selectObjectNode.get("select").textValue();

            String[] splitSelect = previousSelectValue.split("\\.");
            assertThat(splitSelect)
                    .describedAs("Select value needs to be of format TABLE_NAME.COLUMN_ID, value was "
                            + previousSelectValue)
                    .hasSize(TWO);

            String inputDatasetPart = splitSelect[0];
            String remaining = splitSelect[1];

            String newSelectValue = inputDatasetPart + randomisedProductIdentifier + "." + remaining;

            selectObjectNode.put("select", newSelectValue);
        }
    }

    private void randomisePipelineUnionStep(
            final String randomisedProductIdentifier, final JsonNode node) {

        ObjectNode schemaOut = (ObjectNode) node.get(SCHEMA_OUT);

        for (JsonNode join : node.get("unions")) {
            ObjectNode joinNode = (ObjectNode) join;
            ObjectNode unionInSchema = (ObjectNode) join.get("unionInSchema");
            joinNode.set("unionInSchema", randomiseSchemaReference(unionInSchema, randomisedProductIdentifier));
        }

        ObjectNode updatedStep = (ObjectNode) node;
        updatedStep.set(SCHEMA_OUT, randomiseSchemaReference(schemaOut, randomisedProductIdentifier));
    }

    private void randomisePipelineScriptletStep(
            final String randomisedProductIdentifier, final JsonNode node) {

        ObjectNode schemaOut = (ObjectNode) node.get(SCHEMA_OUT);

        for (JsonNode schemaIn : node.get("schemasIn")) {
            ObjectNode scriptletInput = (ObjectNode) schemaIn;
            ObjectNode inputSchema = (ObjectNode) schemaIn.get("inputSchema");
            scriptletInput.set("inputSchema", randomiseSchemaReference(inputSchema, randomisedProductIdentifier));
        }

        ObjectNode updatedStep = (ObjectNode) node;
        updatedStep.set(SCHEMA_OUT, randomiseSchemaReference(schemaOut, randomisedProductIdentifier));
    }

    private ObjectNode randomiseSchemaReference(final ObjectNode schema, final String randomisedProductIdentifier) {
        String randomisedSchemaOutDisplayName = schema.get(DISPLAY_NAME).asText() + randomisedProductIdentifier;
        String randomisedSchemaOutPhysicalName =
                schema.get(PHYSICAL_TABLE_NAME).asText() + randomisedProductIdentifier;
        schema.put(DISPLAY_NAME, randomisedSchemaOutDisplayName);
        schema.put(PHYSICAL_TABLE_NAME, randomisedSchemaOutPhysicalName);
        return schema;
    }

    private byte[] readBytes(final Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read file " + path, e);
        }
    }
}
