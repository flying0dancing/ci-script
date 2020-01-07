package com.lombardrisk.ignis.functional.test.steps.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lombardrisk.ignis.client.design.schema.SchemaDto;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;
import com.lombardrisk.ignis.functional.test.config.properties.ClientProperties;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.StringUtils.substring;

public class TableService {

    private static final int MAX_TABLE_NAME_LENGTH = 31;

    private final ObjectMapper objectMapper;
    private final ClientProperties clientProperties;

    public TableService(
            final ObjectMapper objectMapper,
            final ClientProperties clientProperties) {
        this.objectMapper = objectMapper;
        this.clientProperties = clientProperties;
    }

    public SchemaExport generateSchemaViewWithUniqueName(final String schemaFileName) throws IOException {
        File schemaFile = clientProperties.getRequestsPath().resolve(schemaFileName).toFile();

        SchemaExport newSchema = objectMapper.readValue(schemaFile, SchemaExport.class);
        newSchema.setPhysicalTableName(createUniqueTableName(newSchema.getPhysicalTableName()));

        return newSchema;
    }

    public SchemaDto generateSchemaDtoWithUniqueName(final String schemaFileName) throws IOException {
        File schemaFile = clientProperties.getRequestsPath().resolve(schemaFileName).toFile();

        SchemaDto newSchema = objectMapper.readValue(schemaFile, SchemaDto.class);
        newSchema.setPhysicalTableName(createUniqueTableName(newSchema.getPhysicalTableName()));
        newSchema.setDisplayName(createUniqueTableName(newSchema.getDisplayName()));

        return newSchema;
    }

    private static String createUniqueTableName(final String name) {
        return substring(name + "_" + randomAlphabetic(MAX_TABLE_NAME_LENGTH), 0, MAX_TABLE_NAME_LENGTH)
                .toUpperCase();
    }
}
