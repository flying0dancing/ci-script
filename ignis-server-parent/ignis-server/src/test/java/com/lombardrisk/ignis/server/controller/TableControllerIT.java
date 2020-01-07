package com.lombardrisk.ignis.server.controller;

import com.lombardrisk.ignis.client.core.page.response.Page;
import com.lombardrisk.ignis.client.core.view.PagedView;
import com.lombardrisk.ignis.client.external.productconfig.view.SchemaView;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.table.TableService;
import com.lombardrisk.ignis.server.product.table.model.Table;
import io.vavr.control.Validation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.togglz.core.manager.FeatureManager;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.lombardrisk.ignis.test.config.AdminUser.BASIC_AUTH;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class TableControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TableService tableService;

    @MockBean
    private FeatureManager featureManager;

    @Before
    public void setUp() {
        when(featureManager.isActive(any()))
                .thenReturn(true);
    }

    @Test
    public void getAllTables_ReturnsTables() throws Exception {
         List<SchemaView> tables = Arrays.asList(
                SchemaView.builder()
                        .physicalTableName("SS_44")
                        .displayName("SS_44 display name")
                        .startDate(LocalDate.of(1995, 1, 1))
                        .endDate(null)
                        .version(22)
                        .createdBy("Ignis")
                        .createdTime(new Date())
                        .build(),
                SchemaView.builder()
                        .physicalTableName("TT_44")
                        .displayName("TT_44 display name")
                        .startDate(LocalDate.of(1995, 1, 1))
                        .endDate(LocalDate.of(1995, 8, 7))
                        .version(22)
                        .createdBy("Ignis")
                        .createdTime(new Date())
                        .build()
                );

        when(tableService.getAllTables(any(Pageable.class)))
                .thenReturn(new PagedView<>(tables, new Page(0, 4, 2, 1)));

        mockMvc.perform(
                get("/api/internal/tables")
                        .with(BASIC_AUTH)
                        .contentType(APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].physicalTableName", equalTo("SS_44")))
                .andExpect(jsonPath("$.data[0].startDate", equalTo("1995-01-01")))
                .andExpect(jsonPath("$.data[0].endDate").isEmpty())
                .andExpect(jsonPath("$.data[1].physicalTableName", equalTo("TT_44")))
                .andExpect(jsonPath("$.data[1].startDate", equalTo("1995-01-01")))
                .andExpect(jsonPath("$.data[1].endDate", equalTo("1995-08-07")));
    }

    @Test
    public void get_NoTablesFound_ReturnsEmptyTables() throws Exception {
        when(tableService.getAllTables(any(Pageable.class)))
                .thenReturn(new PagedView<>(emptyList(), new Page(0, 4, 0, 1)));

        mockMvc.perform(
                get("/api/internal/tables")
                        .with(BASIC_AUTH)
                        .contentType(APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", equalTo(0)));
    }

    @Test
    public void deleteTable_CallsTableServiceWithTableId() throws Exception {
        mockMvc.perform(
                delete("/api/internal/tables/123456789")
                        .with(BASIC_AUTH));

        verify(tableService).deleteWithValidation(123456789L);
    }

    @Test
    public void deleteTable_TableDeleted_ReturnsOkResponse() throws Exception {
        Table table = ProductPopulated.table()
                .id(123456789L)
                .build();

        when(tableService.deleteWithValidation(anyLong()))
                .thenReturn(Validation.valid(table));

        mockMvc.perform(
                delete("/api/internal/tables/123456789")
                        .with(BASIC_AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123456789"));
    }

    @Test
    public void deleteTable_CRUDFailure_ReturnsBadRequestResponse() throws Exception {
        when(tableService.deleteWithValidation(anyLong()))
                .thenReturn(Validation.invalid(
                        CRUDFailure.notFoundIds("ProductConfig", 123456789L)));

        mockMvc.perform(
                delete("/api/internal/tables/123456789")
                        .with(BASIC_AUTH))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$[0].errorMessage").value("Could not find ProductConfig for ids [123456789]"));
    }
}
