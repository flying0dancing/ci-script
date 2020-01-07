package com.lombardrisk.ignis.server.product.productconfig;

import com.lombardrisk.ignis.client.external.pipeline.export.PipelineExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;
import com.lombardrisk.ignis.common.ZipUtils;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.server.product.ProductConfigZipObjectMapper;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfigFileContents;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductManifest;
import io.vavr.collection.Seq;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Slf4j
public class ProductConfigFileService {

    private static final String MANIFEST_FILENAME = "manifest.json";
    private static final String PIPELINES_FOLDER = "pipelines/";

    private final ProductConfigZipObjectMapper productConfigZipObjectMapper;

    public ProductConfigFileService(final ProductConfigZipObjectMapper productConfigZipObjectMapper) {
        this.productConfigZipObjectMapper = productConfigZipObjectMapper;
    }

    public Validation<ErrorResponse, ProductConfigFileContents> readProductConfig(final MultipartFile file) throws IOException {
        Map<String, byte[]> files = ZipUtils.readFiles(file.getInputStream());

        if (files.isEmpty()) {
            return Validation.invalid(ProductConfigErrorResponse.productZipEmpty(file.getOriginalFilename()));
        }

        if (!files.containsKey(MANIFEST_FILENAME)) {
            return Validation.invalid(ProductConfigErrorResponse.productMissingManifest(file.getName()));
        }

        Validation<String, ProductManifest> productConfigViewBuilder =
                parseProductManifest(files.get(MANIFEST_FILENAME));

        Validation<String, List<SchemaExport>> tables = parseTableViews(files);
        Validation<String, List<PipelineExport>> pipelineExports = parsePipelineExports(files);

        return productConfigViewBuilder.combine(tables)
                .combine(pipelineExports)
                .ap((builder, tableViews, pipelines) ->
                        ProductConfigFileContents.builder()
                                .productMetadata(builder)
                                .schemas(tableViews)
                                .pipelineExports(pipelines)
                                .build())
                .mapError(Seq::asJava)
                .mapError(ProductConfigErrorResponse::jsonFormatInvalid);
    }

    private Validation<String, ProductManifest> parseProductManifest(final byte[] json) {
        return Try.of(() -> productConfigZipObjectMapper.readProductConfigView(json))
                .onFailure(throwable ->
                        log.error("Unable to parse product manidest from {}", MANIFEST_FILENAME, throwable))
                .map(productConfig -> ProductManifest.builder()
                        .name(productConfig.getName())
                        .version(productConfig.getVersion())
                        .build())
                .toValid(MANIFEST_FILENAME);
    }

    private Validation<String, List<SchemaExport>> parseTableViews(final Map<String, byte[]> files) {
        List<Validation<String, SchemaExport>> allTables = files.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(MANIFEST_FILENAME))
                .filter(entry -> !entry.getKey().startsWith(PIPELINES_FOLDER))
                .map(entry -> parseTableView(entry.getKey(), entry.getValue()))
                .collect(toList());

        List<Validation<String, SchemaExport>> invalidTables = allTables.stream()
                .filter(Validation::isInvalid)
                .collect(toList());

        return handleParsingErrors(allTables, invalidTables);
    }

    private Validation<String, List<PipelineExport>> parsePipelineExports(final Map<String, byte[]> files) {
        List<Validation<String, PipelineExport>> allPipelines = files.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(PIPELINES_FOLDER)
                        && !entry.getKey().equals(PIPELINES_FOLDER))
                .map(entry -> parsePipelineExport(entry.getKey(), entry.getValue()))
                .collect(toList());

        List<Validation<String, PipelineExport>> invalidPipelines = allPipelines.stream()
                .filter(Validation::isInvalid)
                .collect(toList());

        return handleParsingErrors(allPipelines, invalidPipelines);
    }

    private <T> Validation<String, List<T>> handleParsingErrors(
            final List<Validation<String, T>> allValues,
            final List<Validation<String, T>> invalidValues) {

        if (invalidValues.isEmpty()) {
            return Validation.valid(allValues.stream()
                    .filter(Validation::isValid)
                    .map(Validation::get)
                    .collect(toList()));
        }

        return Validation.invalid(invalidValues.stream()
                .map(Validation::getError)
                .collect(Collectors.joining(", ")));
    }

    private Validation<String, SchemaExport> parseTableView(final String fileName, final byte[] json) {
        return Try.of(() -> productConfigZipObjectMapper.readTableView(json))
                .onFailure(throwable -> log.error("Unable to parse schema from {}", fileName, throwable))
                .toValid(fileName);
    }

    private Validation<String, PipelineExport> parsePipelineExport(final String fileName, final byte[] json) {
        return Try.of(() -> productConfigZipObjectMapper.readPipelineView(json))
                .onFailure(throwable -> log.error("Unable to parse pipeline from {}", fileName, throwable))
                .toValid(fileName);
    }
}
