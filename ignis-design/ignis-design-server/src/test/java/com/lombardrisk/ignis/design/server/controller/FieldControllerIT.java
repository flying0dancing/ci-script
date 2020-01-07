package com.lombardrisk.ignis.design.server.controller;

import com.lombardrisk.ignis.client.design.schema.field.FieldDto;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.field.DesignField;
import com.lombardrisk.ignis.design.field.FieldService;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.field.request.FieldRequest;
import com.lombardrisk.ignis.design.server.DesignStudioTestConfig;
import io.vavr.control.Either;
import io.vavr.control.Validation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.booleanFieldRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class FieldControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FieldService fieldService;

    @Test
    public void saveFieldShouldSaveFieldAndReturnOkResponse() throws Exception {
        when(fieldService.save(any(), any()))
                .thenReturn(Either.right(FieldDto.BooleanFieldDto.builder().build()));

        mockMvc.perform(
                post("/api/v1/productConfigs/0/schemas/23/fields")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(booleanFieldRequest()
                                .id(123L)
                                .name("boolean field")
                                .nullable(true)
                                .build())))
                .andExpect(status().isOk());

        ArgumentCaptor<FieldRequest> fieldArgumentCaptor = ArgumentCaptor.forClass(FieldRequest.class);
        verify(fieldService).save(eq(23L), fieldArgumentCaptor.capture());

        FieldRequest savedField = fieldArgumentCaptor.getValue();
        assertThat(savedField.getId()).isEqualTo(123L);
        assertThat(savedField.getName()).isEqualTo("boolean field");
        assertThat(savedField.isNullable()).isTrue();
    }

    @Test
    public void saveField_InvalidField_ShouldReturnBadRequestAndHaveNoInteractionsWithService() throws Exception {
        FieldRequest badRequest =
                booleanFieldRequest()
                        .name(null)
                        .build();

        mockMvc.perform(
                post("/api/v1/productConfigs/0/schemas/23/fields")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest());

        verifyZeroInteractions(fieldService);
    }

    @Test
    public void saveField_SaveFails_ShouldReturnBadRequest() throws Exception {
        when(fieldService.save(any(), any()))
                .thenReturn(Either.left(new ErrorResponse("hello", "there")));

        mockMvc.perform(
                post("/api/v1/productConfigs/0/schemas/23/fields")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(booleanFieldRequest().build())))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteFieldShouldDeleteFieldAndReturnOkResponse() throws Exception {
        Field field = DesignField.Populated.booleanField()
                .id(123456789L)
                .build();

        when(fieldService.deleteWithValidation(anyLong()))
                .thenReturn(Validation.valid(field));

        mockMvc.perform(
                delete("/api/v1/productConfigs/0/schemas/0/fields/123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123456789"));

        verify(fieldService).deleteWithValidation(123456789L);
    }

    @Test
    public void deleteField_FieldNotFound_ShouldReturnBadRequestResponse() throws Exception {
        when(fieldService.deleteWithValidation(anyLong()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("FIELD", singletonList(123456789L))));

        mockMvc.perform(
                delete("/api/v1/productConfigs/0/schemas/0/fields/123456789"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$[0].errorMessage")
                        .value("Could not find FIELD for ids [123456789]"));
    }
}
