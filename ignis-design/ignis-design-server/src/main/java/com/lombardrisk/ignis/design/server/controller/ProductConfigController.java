package com.lombardrisk.ignis.design.server.controller;

import com.lombardrisk.ignis.client.design.path.design;
import com.lombardrisk.ignis.client.design.productconfig.NewProductConfigRequest;
import com.lombardrisk.ignis.client.design.productconfig.ProductConfigDto;
import com.lombardrisk.ignis.client.design.productconfig.UpdateProductConfig;
import com.lombardrisk.ignis.client.design.productconfig.validation.ProductConfigTaskList;
import com.lombardrisk.ignis.client.design.productconfig.validation.ValidationTask;
import com.lombardrisk.ignis.client.external.path.api;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfigImportService;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfigService;
import com.lombardrisk.ignis.design.server.productconfig.converter.ProductConfigConverter;
import com.lombardrisk.ignis.design.server.productconfig.export.ProductConfigExportFileService;
import com.lombardrisk.ignis.design.server.productconfig.export.ProductConfigExportFileService.ProductZipOutputStream;
import com.lombardrisk.ignis.design.server.productconfig.validation.ProductConfigValidator;
import com.lombardrisk.ignis.web.common.response.FcrResponse;
import com.lombardrisk.ignis.web.common.response.ResponseUtils;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static com.lombardrisk.ignis.web.common.exception.GlobalExceptionHandler.UNEXPECTED_ERROR_RESPONSE;
import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
public class ProductConfigController {

    private final ProductConfigService productConfigService;
    private final ProductConfigExportFileService productConfigExportService;
    private final ProductConfigConverter productConfigConverter = new ProductConfigConverter();
    private final ProductConfigImportService productConfigImportService;
    private final ProductConfigValidator productConfigValidator;

    public ProductConfigController(
            final ProductConfigService productConfigService,
            final ProductConfigExportFileService productConfigExportService,
            final ProductConfigImportService productConfigImportService,
            final ProductConfigValidator productConfigValidator) {
        this.productConfigService = productConfigService;
        this.productConfigExportService = productConfigExportService;
        this.productConfigImportService = productConfigImportService;
        this.productConfigValidator = productConfigValidator;
    }

    @GetMapping(path = design.api.v1.ProductConfigs, produces = APPLICATION_JSON_VALUE)
    public List<ProductConfigDto> getProductConfigs() {
        return productConfigService.findAllProductConfigs();
    }

    @GetMapping(path = design.api.v1.productConfigs.ById, produces = APPLICATION_JSON_VALUE)
    public FcrResponse<ProductConfigDto> getOneProductConfig(
            @PathVariable(design.api.Params.PRODUCT_ID) final Long productConfigId) {

        log.info("Get Product [{}]", productConfigId);

        return productConfigService.findOne(productConfigId)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @PostMapping(path = design.api.v1.ProductConfigs, consumes = APPLICATION_JSON_VALUE)
    public FcrResponse<ProductConfigDto> saveProductConfig(
            @Valid @RequestBody final NewProductConfigRequest productConfigRequest) {

        log.info("Add Product [{}]", productConfigRequest.getName());

        return productConfigService.createProductConfig(productConfigRequest)
                .map(productConfigConverter)
                .fold(FcrResponse::badRequest, FcrResponse::okResponse);
    }

    @PatchMapping(path = design.api.v1.productConfigs.ById, consumes = APPLICATION_JSON_VALUE, params = "type=product")
    public FcrResponse<ProductConfigDto> updateProduct(
            @PathVariable(design.api.Params.PRODUCT_ID) final Long productConfigId,
            @Valid @RequestBody final UpdateProductConfig update) {

        log.info("Update Product [{}] with details [{}] ", productConfigId, update);

        return productConfigService.updateProduct(productConfigId, update)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @DeleteMapping(path = design.api.v1.productConfigs.ById)
    public FcrResponse<Identifiable> deleteProductConfig(
            @PathVariable(design.api.Params.PRODUCT_ID) final Long productConfigId) {

        log.info("Delete Product [{}]", productConfigId);

        return productConfigService.deleteById(productConfigId)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @GetMapping(path = {design.api.v1.productConfigs.productId.File,
            design.api.v1.productConfigs.productId.FileByRequiredPipelines})
    public ResponseEntity exportProductConfigUsingRequiredPipelineIds(
            @PathVariable(design.api.Params.PRODUCT_ID) final Long productId,
            @PathVariable(value = design.api.Params.REQUIRED_PIPELINE_IDS, required = false)
            final List<Long> requiredPipelineIds) {

        log.info("Export Product [{}]", productId);

        return Try.withResources(ByteArrayOutputStream::new)
                .of(outputStream -> productConfigExportService.exportProduct(productId, requiredPipelineIds, outputStream))
                .onFailure(throwable -> log.error("Failed to write product configuration to zip", throwable))
                .mapTry(this::mapProductExportResult)
                .getOrElse(failedExportResponse());
    }

    @GetMapping(path = design.api.v1.productConfigs.productId.Tasks)
    public FcrResponse<ProductConfigTaskList> getProductConfigTaskList(
            @PathVariable(design.api.Params.PRODUCT_ID) final Long productId) {

        log.info("Get product task list [{}]", productId);

        return productConfigValidator.productConfigTaskList(productId)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @GetMapping(path = design.api.v1.productConfigs.productId.Validate, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ValidationTask> validate(
            @PathVariable(design.api.Params.PRODUCT_ID) final Long productId) {

        log.info("Validate Product [{}]", productId);

        return productConfigValidator.validateProduct(productId);
    }

    @PostMapping(
            path = design.api.v1.productConfigs.File,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public FcrResponse<Identifiable> importProductConfig(
            @RequestParam(api.Params.FILE) final MultipartFile file) throws IOException {

        log.info("Upload product config file [{}]", file.getOriginalFilename());

        return productConfigImportService.importProductConfig(file.getOriginalFilename(), file.getInputStream())
                .fold(FcrResponse::badRequest, FcrResponse::okResponse);
    }

    private ResponseEntity mapProductExportResult(
            final Validation<CRUDFailure, ProductZipOutputStream<ByteArrayOutputStream>> validation) {

        if (validation.isInvalid()) {
            log.error("Could not export product {}", validation.getError().getErrorMessage());
            return ResponseEntity.badRequest()
                    .body(
                            singletonList(
                                    validation.getError().toErrorResponse()));
        }

        ProductZipOutputStream<ByteArrayOutputStream> productStream = validation.get();

        log.info("Exporting product zip stream");
        return ResponseUtils.toDownloadResponseEntity(
                productStream.getOutputStream(),
                productStream.getZipFilename(),
                "application/zip");
    }

    private static ResponseEntity failedExportResponse() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(singletonList(UNEXPECTED_ERROR_RESPONSE));
    }
}
