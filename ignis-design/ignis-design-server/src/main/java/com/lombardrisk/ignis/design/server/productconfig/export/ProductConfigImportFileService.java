package com.lombardrisk.ignis.design.server.productconfig.export;

import com.lombardrisk.ignis.client.design.productconfig.NewProductConfigRequest;
import com.lombardrisk.ignis.client.external.pipeline.export.PipelineExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;
import com.lombardrisk.ignis.common.ZipUtils;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import io.vavr.collection.Seq;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.lombardrisk.ignis.design.server.productconfig.export.ProductConfigExportFileService.PIPELINE_FOLDER;
import static java.util.stream.Collectors.toList;

@Slf4j
public class ProductConfigImportFileService {

    private static final String MANIFEST_FILENAME = "manifest.json";
    private static final String UNABLE_TO_PARSE = "Unable to parse {}";

    private final ProductConfigZipObjectMapper productConfigZipObjectMapper;

    public ProductConfigImportFileService(final ProductConfigZipObjectMapper productConfigZipObjectMapper) {
        this.productConfigZipObjectMapper = productConfigZipObjectMapper;
    }

    public Validation<ErrorResponse, ImportProductRequest> readProductConfig(
            final String productFileName, final InputStream inputStream) throws IOException {
        Map<String, byte[]> files = ZipUtils.readFiles(inputStream);

        if (files.isEmpty()) {
            return Validation.invalid(ProductConfigErrorResponse.productZipEmpty(productFileName));
        }

        if (!files.containsKey(MANIFEST_FILENAME)) {
            return Validation.invalid(ProductConfigErrorResponse.productMissingManifest(productFileName));
        }

        Validation<String, NewProductConfigRequest> productConfigViewBuilder =
                parseProductManifest(files.get(MANIFEST_FILENAME));

        Validation<String, List<SchemaExport>> tables = parseTableViews(files);
        Validation<String, List<PipelineExport>> pipelines = parsePipelineViews(files);

        return Validation.combine(productConfigViewBuilder, tables, pipelines)
                .ap((newProductConfigRequest, schemaExports, pipelineExports) ->
                        ImportProductRequest.builder()
                                .newProductConfigRequest(newProductConfigRequest)
                                .schemaExports(schemaExports)
                                .pipelineExports(pipelineExports)
                                .build())
                .mapError(Seq::asJava)
                .mapError(ProductConfigErrorResponse::jsonFormatInvalid);
    }

    private Validation<String, NewProductConfigRequest> parseProductManifest(final byte[] json) {
        return Try.of(() -> productConfigZipObjectMapper.readProductConfigView(json))
                .onFailure(throwable -> log.error(UNABLE_TO_PARSE, MANIFEST_FILENAME, throwable))
                .map(productConfig -> NewProductConfigRequest.builder()
                        .name(productConfig.getName())
                        .version(productConfig.getVersion())
                        .build())
                .toValid(MANIFEST_FILENAME);
    }

    private Validation<String, List<SchemaExport>> parseTableViews(final Map<String, byte[]> files) {
        List<Validation<String, SchemaExport>> allTables = files.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(MANIFEST_FILENAME))
                .filter(entry -> !entry.getKey().startsWith(PIPELINE_FOLDER))
                .map(entry -> parseTableView(entry.getKey(), entry.getValue()))
                .collect(toList());

        List<Validation<String, SchemaExport>> invalidTables = allTables.stream()
                .filter(Validation::isInvalid)
                .collect(toList());

        if (invalidTables.isEmpty()) {
            return Validation.valid(allTables.stream()
                    .filter(Validation::isValid)
                    .map(Validation::get)
                    .collect(toList()));
        }

        return Validation.invalid(invalidTables.stream()
                .map(Validation::getError)
                .collect(Collectors.joining(", ")));
    }

    private Validation<String, SchemaExport> parseTableView(final String fileName, final byte[] json) {
        return Try.of(() -> productConfigZipObjectMapper.readTableView(json))
                .onFailure(throwable -> log.error(UNABLE_TO_PARSE, fileName, throwable))
                .toValid(fileName);
    }

    private Validation<String, List<PipelineExport>> parsePipelineViews(final Map<String, byte[]> files) {
        List<Validation<String, PipelineExport>> allPipelines = files.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(MANIFEST_FILENAME))
                .filter(entry -> entry.getKey().startsWith(PIPELINE_FOLDER + "/"))
                .map(entry -> parsePipelineView(entry.getKey(), entry.getValue()))
                .collect(toList());

        List<String> invalidPipelineFiles = allPipelines.stream()
                .filter(Validation::isInvalid)
                .map(Validation::getError)
                .collect(toList());

        if (!invalidPipelineFiles.isEmpty()) {
            return Validation.invalid(String.join(", ", invalidPipelineFiles));
        }

        return Validation.valid(allPipelines.stream()
                .map(Validation::get)
                .collect(toList()));
    }

    private Validation<String, PipelineExport> parsePipelineView(final String fileName, final byte[] json) {
        return Try.of(() -> productConfigZipObjectMapper.readPipelineView(json))
                .onFailure(throwable -> log.error(UNABLE_TO_PARSE, fileName, throwable))
                .toValid(fileName);
    }
}
