package com.lombardrisk.ignis.server.controller;

import com.lombardrisk.ignis.client.core.view.PagedView;
import com.lombardrisk.ignis.client.external.productconfig.view.SchemaView;
import com.lombardrisk.ignis.client.internal.path.api;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.server.product.table.TableService;
import com.lombardrisk.ignis.web.common.response.FcrResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
public class TableController {

    private final TableService tableService;

    public TableController(
            final TableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping(path = api.internal.Tables, produces = APPLICATION_JSON_VALUE)
    @Transactional
    public FcrResponse<PagedView> getAllTables(
            @PageableDefault(size = Integer.MAX_VALUE) final Pageable pageable) {
        log.trace("Find all tables");
        PagedView<SchemaView> allTables = tableService.getAllTables(pageable);
        return FcrResponse.okResponse(allTables);
    }

    @DeleteMapping(api.internal.tables.ById)
    public FcrResponse<Identifiable> deleteTable(@PathVariable(api.Params.ID) final Long id) {
        log.info("Delete schema with id [{}]", id);
        return tableService.deleteWithValidation(id)
                .map(Identifiable::toIdentifiable)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }
}
