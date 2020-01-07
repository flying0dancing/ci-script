package com.lombardrisk.ignis.design.server.productconfig.export;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lombardrisk.ignis.common.ZipUtils;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static java.util.Collections.emptyList;

@UtilityClass
public class ProductExportTestUtils {

    public static ByteArrayInputStream productToInputStream(final String manifestContent) throws Exception {
        return productToInputStream(manifestContent, emptyList());
    }

    public static ByteArrayInputStream productToInputStream(
            final String manifestContent,
            final List<ObjectNode> schemas) throws Exception {

        return productToInputStream(manifestContent, schemas, emptyList());
    }

    public static ByteArrayInputStream productToInputStream(
            final String manifestContent,
            final List<ObjectNode> schemas,
            final List<ObjectNode> pipelines) throws Exception {

        Map<String, byte[]> fileNameToContent = new HashMap<>();

        addProductManifest(manifestContent, fileNameToContent);
        addSchemas(schemas, fileNameToContent);
        addPipelines(pipelines, fileNameToContent);

        byte[] productBytes;

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            ZipUtils.writeFiles(fileNameToContent, byteArrayOutputStream);
            productBytes = byteArrayOutputStream.toByteArray();
        }

        return new ByteArrayInputStream(productBytes);
    }

    private static void addProductManifest(final String manifestContent, final Map<String, byte[]> fileNameToContent) {
        fileNameToContent.put("manifest.json", manifestContent.getBytes());
    }

    private static void addSchemas(
            final List<ObjectNode> schemas,
            final Map<String, byte[]> fileNameToContent) throws JsonProcessingException {

        for (ObjectNode schema : schemas) {
            String fileName = String.format(
                    "%s_%s_%s.json",
                    schema.get("physicalTableName").textValue(),
                    schema.get("version").intValue(),
                    RandomStringUtils.randomAlphanumeric(10));

            fileNameToContent.put(
                    fileName, MAPPER.writeValueAsString(schema).getBytes());
        }
    }

    private static void addPipelines(
            final List<ObjectNode> pipelines,
            final Map<String, byte[]> fileNameToContent) throws JsonProcessingException {

        for (ObjectNode pipeline : pipelines) {
            String fileName = String.format("pipelines/%s_%s.json",
                    pipeline.get("name").textValue(), RandomStringUtils.randomAlphanumeric(10));
            fileNameToContent.put(fileName, MAPPER.writeValueAsBytes(pipeline));
        }
    }
}
