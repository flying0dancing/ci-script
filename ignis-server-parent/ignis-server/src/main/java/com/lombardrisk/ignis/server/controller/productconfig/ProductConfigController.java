package com.lombardrisk.ignis.server.controller.productconfig;

import com.lombardrisk.ignis.client.external.path.api;
import com.lombardrisk.ignis.client.external.path.api.Params;
import com.lombardrisk.ignis.client.external.productconfig.view.ProductConfigView;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.server.job.product.ProductConfigImportJobService;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigFileService;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigService;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfigFileContents;
import com.lombardrisk.ignis.web.common.response.BadRequestErrorResponse;
import com.lombardrisk.ignis.web.common.response.FcrResponse;
import io.micrometer.core.annotation.Timed;
import io.vavr.control.Validation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
public class ProductConfigController {

    private final ProductConfigService productConfigService;
    private final ProductConfigFileService productConfigFileService;
    private final ProductConfigImportJobService productConfigImporter;

    public ProductConfigController(
            final ProductConfigService productConfigService,
            final ProductConfigFileService productConfigFileService,
            final ProductConfigImportJobService productConfigImporter) {
        this.productConfigService = productConfigService;
        this.productConfigFileService = productConfigFileService;
        this.productConfigImporter = productConfigImporter;
    }

    @GetMapping(path = api.external.v1.ProductConfigs, produces = APPLICATION_JSON_VALUE)
    @Timed("ignis.get_product_configs")
    public FcrResponse<List<ProductConfigView>> getProductConfigs() {
        log.debug("Find all product configs");

        return FcrResponse.okResponse(productConfigService.findAllViewsWithoutFieldsRulesAndSteps());
    }

    @GetMapping(path = api.external.v1.productConfigs.ById, produces = APPLICATION_JSON_VALUE)
    @Timed("ignis.get_product_config")
    public FcrResponse<ProductConfigView> getProductConfig(
            @PathVariable(Params.ID) final Long productId) {
        return productConfigService.findView(productId)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @PostMapping(
            path = api.external.v1.productConfigs.File,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed("ignis.import_product_config")
    public ResponseEntity importProductConfig(
            @RequestParam(Params.FILE) final MultipartFile file) throws IOException {

        log.info("Upload product config file [{}]", file.getOriginalFilename());

        Validation<ErrorResponse, ProductConfigFileContents> importResult =
                productConfigFileService.readProductConfig(file);

        if (importResult.isInvalid()) {
            return BadRequestErrorResponse.valueOf(importResult.getError());
        }

        String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();

        return productConfigImporter
                .importProductConfig(importResult.get(), currentUserName)
                .map(Identifiable::toIdentifiable)
                .fold(BadRequestErrorResponse::valueOf, ResponseEntity::ok);
    }

    @DeleteMapping(path = api.external.v1.productConfigs.ById)
    @Timed("ignis.delete_product_config")
    public ResponseEntity deleteProductConfig(@PathVariable(Params.ID) final Long productConfigId) {
        log.info("Remove product config with id [{}]", productConfigId);

        String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();

        return productConfigImporter
                .rollbackProductConfig(productConfigId, currentUserName)
                .map(Identifiable::toIdentifiable)
                .fold(BadRequestErrorResponse::valueOf, ResponseEntity::ok);
    }
}
