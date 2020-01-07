package com.lombardrisk.ignis.design.server.productconfig.export;

import com.lombardrisk.ignis.client.external.pipeline.export.PipelineExport;
import com.lombardrisk.ignis.client.external.productconfig.export.ProductConfigExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.common.ZipUtils;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.server.pipeline.model.Pipeline;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfigService;
import com.lombardrisk.ignis.design.server.productconfig.api.ProductPipelineRepository;
import com.lombardrisk.ignis.design.server.productconfig.export.converter.PipelineExportConverter;
import com.lombardrisk.ignis.design.server.productconfig.export.converter.ProductConfigExportConverter;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductConfigExportFileService {

    private static final String MANIFEST_FILENAME = "manifest.json";
    static final String PIPELINE_FOLDER = "pipelines";

    private final ProductPipelineRepository pipelineRepository;
    private final ProductConfigExportConverter productConfigConverter;
    private final ProductConfigZipObjectMapper productConfigZipObjectMapper;
    private final ProductConfigService productConfigService;
    private final PipelineExportConverter
            pipelineExportConverter = new PipelineExportConverter();

    public ProductConfigExportFileService(
            final ProductPipelineRepository pipelineRepository,
            final ProductConfigExportConverter productConfigConverter,
            final ProductConfigZipObjectMapper productConfigZipObjectMapper,
            final ProductConfigService productConfigService) {

        this.pipelineRepository = pipelineRepository;
        this.productConfigConverter = productConfigConverter;
        this.productConfigZipObjectMapper = productConfigZipObjectMapper;
        this.productConfigService = productConfigService;
    }

    public <T extends OutputStream> Validation<CRUDFailure, ProductZipOutputStream<T>> exportProduct(
            final long productId,
            final T outputStream) throws IOException {

        return exportProduct(productId, Arrays.asList(), outputStream);
    }

    public <T extends OutputStream> Validation<CRUDFailure, ProductZipOutputStream<T>> exportProduct(
            final long productId,
            final List<Long> requiredPipelineIds,
            final T outputStream) throws IOException {

        Validation<CRUDFailure, ProductConfig> productConfigValidation = productConfigService
                .findWithValidation(productId);

        if (productConfigValidation.isInvalid()) {
            return Validation.invalid(productConfigValidation.getError());
        }

        ProductConfig productConfig = productConfigValidation.get();

        List<Pipeline> pipelines = pipelineRepository.findAllByProductId(productId);

        if (requiredPipelineIds == null) {
            pipelines = Arrays.asList();
        } else if (requiredPipelineIds.size() > 0) {
            pipelines = pipelines.stream()
                    .filter(pipeline -> requiredPipelineIds.contains(pipeline.getId()))
                    .collect(Collectors.toList());
        }

        List<PipelineExport> pipelinesToExport = MapperUtils.map(
                pipelines,
                pipeline -> pipelineExportConverter.apply(pipeline, productConfig));

        String zipFilename = String.format("%s_%s.zip", productConfig.getName(), productConfig.getVersion());

        T productOutput = writeProductConfig(productConfig, pipelinesToExport, outputStream);
        return Validation.valid(new ProductZipOutputStream<>(zipFilename, productOutput));
    }

    <T extends OutputStream> T writeProductConfig(
            final ProductConfig productConfig,
            final List<PipelineExport> pipelines,
            final T outputStream) throws IOException {

        ProductConfigExport productConfigView = productConfigConverter.apply(productConfig);
        Map<String, byte[]> files = new HashMap<>();

        files.put(MANIFEST_FILENAME, productConfigZipObjectMapper.writeProductConfigView(productConfigView));

        for (final SchemaExport schemaView : productConfigView.getTables()) {
            String filename = String.format("%s_%s.json", schemaView.getPhysicalTableName(), schemaView.getVersion());

            files.put(filename, productConfigZipObjectMapper.writeTableExport(schemaView));
        }

        for (PipelineExport pipeline : pipelines) {
            String filename = String.format("%s/%s.json", PIPELINE_FOLDER, pipeline.getName());
            files.put(filename, productConfigZipObjectMapper.writePipelineExport(pipeline));
        }

        ZipUtils.writeFiles(files, (OutputStream) outputStream);

        return outputStream;
    }

    @AllArgsConstructor
    @Data
    public static class ProductZipOutputStream<T extends OutputStream> {

        private final String zipFilename;
        private final T outputStream;
    }
}
