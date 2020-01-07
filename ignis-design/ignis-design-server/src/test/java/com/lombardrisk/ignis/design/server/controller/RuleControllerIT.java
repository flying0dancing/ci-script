package com.lombardrisk.ignis.design.server.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.client.design.RuleExample;
import com.lombardrisk.ignis.client.design.ValidationRuleRequest;
import com.lombardrisk.ignis.client.external.rule.ExampleField;
import com.lombardrisk.ignis.client.external.rule.TestResultType;
import com.lombardrisk.ignis.design.server.DesignStudioTestConfig;
import com.lombardrisk.ignis.design.server.fixtures.Design;
import com.lombardrisk.ignis.design.server.productconfig.schema.RuleService;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import io.vavr.control.Either;
import io.vavr.control.Validation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static com.lombardrisk.ignis.client.design.fixtures.Populated.validationRuleRequest;
import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class RuleControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RuleService ruleSetService;

    @Test
    public void saveRule_ValidRule_ReturnsOkResponse() throws Exception {
        when(ruleSetService.saveValidationRule(any(), any()))
                .thenReturn(Either.right(Design.Populated.validationRule().build()));

        mockMvc.perform(
                post("/api/v1/productConfigs/0/schemas/23/rules")

                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(validationRuleRequest()
                                .ruleId("this is the rule id")
                                .name("this is the rule name")
                                .expression("this is the expression")
                                .build())))
                .andExpect(status().isOk());

        ArgumentCaptor<ValidationRule> ruleArgumentCaptor = ArgumentCaptor.forClass(ValidationRule.class);
        verify(ruleSetService).saveValidationRule(eq(23L), ruleArgumentCaptor.capture());

        ValidationRule savedRule = ruleArgumentCaptor.getValue();
        assertThat(savedRule.getRuleId()).isEqualTo("this is the rule id");
        assertThat(savedRule.getName()).isEqualTo("this is the rule name");
        assertThat(savedRule.getExpression()).isEqualTo("this is the expression");
    }

    @Test
    public void saveRule_InvalidRuleRequest_ReturnsBadRequestResponse() throws Exception {
        ValidationRuleRequest badRequest =
                validationRuleRequest()
                        .name(null)
                        .build();

        mockMvc.perform(
                post("/api/v1/productConfigs/0/schemas/23/rules")

                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest());

        verifyZeroInteractions(ruleSetService);
    }

    @Test
    public void saveRule_InvalidRule_ReturnsBadRequestResponse() throws Exception {
        when(ruleSetService.saveValidationRule(any(), any()))
                .thenReturn(Either.left(singletonList(new ErrorResponse("hello", "there"))));

        mockMvc.perform(
                post("/api/v1/productConfigs/0/schemas/23/rules")

                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(validationRuleRequest().build())))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteRule_ValidRule_ReturnsOkResponse() throws Exception {
        ValidationRule validationRule = Design.Populated.validationRule()
                .id(123456789L)
                .build();

        when(ruleSetService.deleteWithValidation(anyLong()))
                .thenReturn(Validation.valid(validationRule));

        mockMvc.perform(
                delete("/api/v1/productConfigs/0/schemas/0/rules/123456789")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123456789"));
    }

    @Test
    public void deleteRule_InvalidRule_ReturnsBadRequestResponse() throws Exception {
        when(ruleSetService.deleteWithValidation(anyLong()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("VALIDATION_RULE", singletonList(123456789L))));

        mockMvc.perform(
                delete("/api/v1/productConfigs/0/schemas/0/rules/123456789")
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$[0].errorMessage")
                        .value("Could not find VALIDATION_RULE for ids [123456789]"));
    }

    @Test
    public void getExamples_NonExistingRule_ReturnsEmptyResponse() throws Exception {
        when(ruleSetService.testRuleExamples(any()))
                .thenReturn(emptyList());

        mockMvc.perform(
                get("/api/v1/productConfigs/0/schemas/0/rules/000/examples")
                        .contentType(APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().string("[]"));
    }

    @Test
    public void getExamples_ExistingRuleId_ReturnsResponse() throws Exception {
        when(ruleSetService.testRuleExamples(any()))
                .thenReturn(ImmutableList.of(
                        RuleExample.builder()
                                .id(321L)
                                .expectedResult(TestResultType.Pass)
                                .actualResult(TestResultType.Fail)
                                .unexpectedError("null operand")
                                .exampleFields(ImmutableMap.of(
                                        "FIELD_1",
                                        ExampleField.builder()
                                                .value("22.45")
                                                .error("is not nullable")
                                                .build()))
                                .build()));

        mockMvc.perform(
                get("/api/v1/productConfigs/0/schemas/0/rules/320/examples")
                        .contentType(APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("321"))
                .andExpect(jsonPath("$[0].expectedResult").value("Pass"))
                .andExpect(jsonPath("$[0].actualResult").value("Fail"))
                .andExpect(jsonPath("$[0].unexpectedError").value("null operand"))
                .andExpect(jsonPath("$[0].exampleFields.FIELD_1.value").value("22.45"))
                .andExpect(jsonPath("$[0].exampleFields.FIELD_1.error").value("is not nullable"))
        ;
    }
}
