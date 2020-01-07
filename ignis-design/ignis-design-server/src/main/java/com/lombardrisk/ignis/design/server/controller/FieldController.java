package com.lombardrisk.ignis.design.server.controller;

import com.lombardrisk.ignis.client.design.schema.field.FieldDto;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.design.field.FieldService;
import com.lombardrisk.ignis.design.field.request.FieldRequest;
import com.lombardrisk.ignis.web.common.response.FcrResponse;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.lombardrisk.ignis.client.design.path.design.api;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(produces = APPLICATION_JSON_VALUE)
public class FieldController {

    private final FieldService fieldService;

    public FieldController(final FieldService fieldService) {
        this.fieldService = fieldService;
    }

    @PostMapping(path = api.v1.productConfigs.productId.schemas.schemaId.Fields)
    public FcrResponse<FieldDto> saveField(
            @PathVariable(api.Params.SCHEMA_ID) final Long schemaId,
            @Valid @RequestBody final FieldRequest fieldRequest) {
        log.info("Save field [{}] for schema with id [{}]", fieldRequest.getName(), schemaId);

        Either<ErrorResponse, FieldDto> eitherField =
                fieldService.save(schemaId, fieldRequest);

        return eitherField
                .fold(FcrResponse::badRequest, FcrResponse::okResponse);
    }

    @PutMapping(path = api.v1.productConfigs.productId.schemas.schemaId.fields.ById)
    public FcrResponse<FieldDto> updateField(
            @PathVariable(api.Params.FIELD_ID) final Long fieldId,
            @Valid @RequestBody final FieldRequest fieldRequest) {
        log.info("Update field [{}] for id [{}]", fieldRequest.getName(), fieldId);

        return fieldService.update(fieldId, fieldRequest)
                .fold(FcrResponse::badRequest, FcrResponse::okResponse);
    }

    @DeleteMapping(path = api.v1.productConfigs.productId.schemas.schemaId.fields.ById)
    public FcrResponse<Identifiable> deleteField(@PathVariable(api.Params.FIELD_ID) final Long id) {
        log.info("Delete field with id [{}]", id);

        return fieldService.deleteWithValidation(id)
                .map(Identifiable::toIdentifiable)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }
}
