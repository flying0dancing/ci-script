package com.lombardrisk.ignis.functional.test.steps.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvFactory;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.client.design.pipeline.PipelineStepRequest;
import com.lombardrisk.ignis.client.design.schema.SchemaDto;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto;
import com.lombardrisk.ignis.functional.test.config.properties.ClientProperties;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PipelineService {
    private final ObjectMapper objectMapper;
    private final CsvMapper csvMapper;
    private final ClientProperties clientProperties;

    public PipelineService(
            final ObjectMapper objectMapper,
            final ClientProperties clientProperties) {
        this.objectMapper = objectMapper;
        this.clientProperties = clientProperties;
        this.csvMapper = new CsvMapper(new CsvFactory(objectMapper));
    }

    public PipelineStepRequest generatePipelineStepWithUniqueName(final String pipelineStepFileName) throws IOException {
        File pipelineStepFile = clientProperties.getRequestsPath().resolve(pipelineStepFileName).toFile();
        return objectMapper.readValue(pipelineStepFile, PipelineStepRequest.class);
    }

    public List<Map<Long, String>> readCsvData(
            final SchemaDto schemaDto,
            final String pathToCsvFile) throws IOException {
        File resolvedFile = clientProperties.getRequestsPath().resolve(pathToCsvFile).toFile();

        Map<String, FieldDto> fieldNameLookup = schemaDto.getFields().stream()
                .collect(Collectors.toMap(FieldDto::getName, Function.identity()));

        CsvSchema.Builder csvSchemaBuilder = CsvSchema.builder();
        for (FieldDto field : schemaDto.getFields()) {
            csvSchemaBuilder.addColumn(field.getName());
        }

        CsvSchema csvSchema = csvSchemaBuilder.build();
        List<Map<String, String>> rows = ImmutableList.copyOf(
                csvMapper.readerFor(Map.class)
                        .with(csvSchema)
                        .readValues(resolvedFile));

        return rows.stream()
                .skip(1)
                .map(rowMap -> {
                    Map<Long, String> newMap = new HashMap<>();
                    for (Map.Entry<String, String> column : rowMap.entrySet()) {

                        String columnHeader = column.getKey();
                        String columnValue = column.getValue().trim();
                        FieldDto fieldDto = fieldNameLookup.get(columnHeader);
                        newMap.put(fieldDto.getId(), columnValue);
                    }
                    return newMap;
                }).collect(Collectors.toList());


    }
}
