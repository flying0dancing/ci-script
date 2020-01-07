package com.lombardrisk.ignis.design.server.controller;

import com.lombardrisk.ignis.client.design.RuleExample;
import com.lombardrisk.ignis.client.design.ValidationRuleRequest;
import com.lombardrisk.ignis.client.design.path.design.api;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.design.server.productconfig.converter.ValidationRuleRequestConverter;
import com.lombardrisk.ignis.design.server.productconfig.export.converter.ValidationRuleExportConverter;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import com.lombardrisk.ignis.design.server.productconfig.schema.RuleService;
import com.lombardrisk.ignis.web.common.response.BadRequestErrorResponse;
import com.lombardrisk.ignis.web.common.response.FcrResponse;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(produces = APPLICATION_JSON_VALUE)
public class RuleController {

    private final RuleService ruleService;

    private final ValidationRuleRequestConverter ruleRequestConverter;
    private final ValidationRuleExportConverter ruleViewConverter;

    public RuleController(
            final RuleService ruleService,
            final ValidationRuleRequestConverter validationRuleRequestConverter,
            final ValidationRuleExportConverter validationRuleConverter) {
        this.ruleService = ruleService;
        this.ruleRequestConverter = validationRuleRequestConverter;
        this.ruleViewConverter = validationRuleConverter;
    }

    @PostMapping(path = api.v1.productConfigs.productId.schemas.schemaId.Rules)
    public ResponseEntity saveRule(
            @PathVariable(api.Params.PRODUCT_ID) final Long productId,
            @PathVariable(api.Params.SCHEMA_ID) final Long tableId,
            @Valid @RequestBody final ValidationRuleRequest ruleRequest) {
        log.debug("Save validation rule [{}] for table [{}]", ruleRequest.getName(), tableId);

        ValidationRule rule = ruleRequestConverter.apply(ruleRequest);

        Either<List<ErrorResponse>, ValidationRule> eitherValidationRule =
                ruleService.saveValidationRule(tableId, rule);

        return eitherValidationRule
                .map(ruleViewConverter)
                .fold(BadRequestErrorResponse::valueOf, ResponseEntity::ok);
    }

    @DeleteMapping(path = api.v1.productConfigs.productId.schemas.schemaId.rules.ById)
    public FcrResponse<Identifiable> deleteRule(
            @PathVariable(api.Params.PRODUCT_ID) final Long productId,
            @PathVariable(api.Params.SCHEMA_ID) final Long tableId,
            @PathVariable(api.Params.RULE_ID) final Long id) {

        return ruleService.deleteWithValidation(id)
                .map(Identifiable::toIdentifiable)
                .peek(rule -> log.info("Delete rule with id [{}]", rule.getId()))
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @GetMapping(path = api.v1.productConfigs.productId.schemas.schemaId.rules.ruleId.Examples)
    public List<RuleExample> getExamples(
            @PathVariable(api.Params.PRODUCT_ID) final Long productId,
            @PathVariable(api.Params.SCHEMA_ID) final Long tableId,
            @NotNull @PathVariable(api.Params.RULE_ID) final Long id) {
        log.info("Find examples for rule with id [{}]", id);

        return ruleService.testRuleExamples(id);
    }
}
