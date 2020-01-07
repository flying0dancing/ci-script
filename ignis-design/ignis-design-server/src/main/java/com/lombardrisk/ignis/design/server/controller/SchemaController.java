package com.lombardrisk.ignis.design.server.controller;

import com.lombardrisk.ignis.client.design.path.design;
import com.lombardrisk.ignis.client.design.schema.CopySchemaRequest;
import com.lombardrisk.ignis.client.design.schema.NewSchemaVersionRequest;
import com.lombardrisk.ignis.client.design.schema.SchemaDto;
import com.lombardrisk.ignis.client.design.schema.UpdateSchema;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfigService;
import com.lombardrisk.ignis.design.server.productconfig.converter.SchemaConverter;
import com.lombardrisk.ignis.design.server.productconfig.schema.SchemaService;
import com.lombardrisk.ignis.design.server.productconfig.schema.SchemaService.SchemaCsvOutputStream;
import com.lombardrisk.ignis.design.server.productconfig.schema.request.CreateSchemaRequest;
import com.lombardrisk.ignis.web.common.response.FcrResponse;
import com.lombardrisk.ignis.web.common.response.ResponseUtils;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;

import static com.lombardrisk.ignis.web.common.exception.GlobalExceptionHandler.UNEXPECTED_ERROR_RESPONSE;
import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
public class SchemaController {

    private final SchemaService schemaService;
    private final ProductConfigService productConfigService;
    private final SchemaConverter schemaConverter;

    public SchemaController(
            final SchemaService schemaService,
            final ProductConfigService productConfigService,
            final SchemaConverter schemaConverter) {
        this.schemaService = schemaService;
        this.productConfigService = productConfigService;
        this.schemaConverter = schemaConverter;
    }

    @PatchMapping(path = design.api.v1.productConfigs.productId.schemas.ById, consumes = APPLICATION_JSON_VALUE)
    public FcrResponse<Identifiable> updateSchema(
            @PathVariable(value = design.api.Params.PRODUCT_ID) final Long productId,
            @PathVariable(value = design.api.Params.SCHEMA_ID) final Long schemaId,
            @Valid @RequestBody final UpdateSchema update) {
        log.debug("Update schema properties [{}]", schemaId);

        return schemaService.updateWithoutFields(productId, schemaId, update)
                .map(Identifiable::toIdentifiable)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @PostMapping(path = design.api.v1.productConfigs.productId.Schemas, consumes = APPLICATION_JSON_VALUE)
    public FcrResponse<SchemaDto> addSchemaToProduct(
            @PathVariable(design.api.Params.PRODUCT_ID) final Long productConfigId,
            @Valid @RequestBody final CreateSchemaRequest createSchemaRequest) {

        log.info("Create schema [{}] in product [{}]",
                createSchemaRequest.getVersionedName(), productConfigId);

        return productConfigService.createNewSchemaOnProduct(productConfigId, createSchemaRequest)
                .map(schemaConverter)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @PostMapping(path = design.api.v1.productConfigs.productId.schemas.ById, consumes = APPLICATION_JSON_VALUE)
    public FcrResponse<SchemaDto> createNewVersion(
            @PathVariable(design.api.Params.PRODUCT_ID) final Long productConfigId,
            @PathVariable(design.api.Params.SCHEMA_ID) final Long schemaId,
            @RequestBody final NewSchemaVersionRequest newSchemaVersionRequest) {

        log.info("Create next version of schema [{}] from product [{}] with start date [{}]",
                schemaId, productConfigId, newSchemaVersionRequest.getStartDate());

        return productConfigService.createNewSchemaVersion(productConfigId, schemaId, newSchemaVersionRequest)
                .map(schemaConverter)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @PostMapping(path = design.api.v1.productConfigs.productId.schemas.Copy, consumes = APPLICATION_JSON_VALUE)
    public FcrResponse<SchemaDto> copy(
            @PathVariable(design.api.Params.PRODUCT_ID) final Long productConfigId,
            @PathVariable(design.api.Params.SCHEMA_ID) final Long schemaId,
            @RequestBody final CopySchemaRequest copySchemaRequest) {

        log.info("Copy schema [{}]", schemaId);

        return schemaService.copySchema(productConfigId, schemaId, copySchemaRequest)
                .map(schemaConverter)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @GetMapping(path = design.api.v1.productConfigs.productId.schemas.ById, produces = APPLICATION_JSON_VALUE)
    public FcrResponse<SchemaDto> getTableById(
            @PathVariable(value = design.api.Params.PRODUCT_ID, required = false) final Long productId,
            @PathVariable(design.api.Params.SCHEMA_ID) final Long id) {

        log.info("Find table with id [{}]", id);

        return schemaService.findByProductIdAndId(productId, id)
                .map(schemaConverter)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @GetMapping(path = design.api.v1.productConfigs.productId.schemas.ById
            + "/exampleCsv", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity createExampleCsv(
            @PathVariable(value = design.api.Params.PRODUCT_ID, required = false) final Long productId,
            @PathVariable(design.api.Params.SCHEMA_ID) final Long id) {

        log.info("Create example csv file for schema with id [{}]", id);

        return Try.withResources(ByteArrayOutputStream::new)
                .of(outputStream -> schemaService.createExampleCsv(id, outputStream))
                .onFailure(throwable -> log.error("Failed to create example csv", throwable))
                .mapTry(this::mapSchemaCsvDownloadValid)
                .getOrElse(failedExportResponse());
    }

    @DeleteMapping(path = design.api.v1.productConfigs.productId.schemas.ById, produces = APPLICATION_JSON_VALUE)
    public FcrResponse<Identifiable> delete(
            @PathVariable(value = design.api.Params.PRODUCT_ID, required = false) final Long productId,
            @PathVariable(design.api.Params.SCHEMA_ID) final Long id) {

        log.trace("Find table with id [{}]", id);

        return productConfigService.removeSchemaFromProduct(productId, id)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    private ResponseEntity mapSchemaCsvDownloadValid(
            final Validation<ErrorResponse, SchemaCsvOutputStream<ByteArrayOutputStream>> validation) {

        if (validation.isInvalid()) {
            return ResponseEntity.badRequest()
                    .body(
                            singletonList(
                                    validation.getError()));
        }

        return ResponseUtils.toDownloadResponseEntity(
                validation.get().getOutputStream(), validation.get().getFilename(), "text/csv");
    }

    private static ResponseEntity failedExportResponse() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(singletonList(UNEXPECTED_ERROR_RESPONSE));
    }
}
