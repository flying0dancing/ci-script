package com.lombardrisk.ignis.server.product.table;

import com.lombardrisk.ignis.client.core.view.PagedView;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaPeriod;
import com.lombardrisk.ignis.client.external.productconfig.view.SchemaView;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.data.common.service.JpaCRUDService;
import com.lombardrisk.ignis.server.product.table.model.Field;
import com.lombardrisk.ignis.server.product.table.model.Table;
import com.lombardrisk.ignis.server.product.table.view.TableToSchemaWithRulesView;
import com.lombardrisk.ignis.server.product.util.PageToViewConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Transactional
@Slf4j
public class TableService implements JpaCRUDService<Table> {

    private final TableRepository tableRepository;
    private final TimeSource timeSource;
    private final FieldRepository fieldRepository;
    private final PageToViewConverter pageToViewConverter;
    private final TableToSchemaWithRulesView tableToSchemaWithRulesView;

    public TableService(
            final TableRepository tableRepository,
            final TimeSource timeSource,
            final FieldRepository fieldRepository,
            final PageToViewConverter pageToViewConverter,
            final TableToSchemaWithRulesView tableToSchemaWithRulesView) {
        this.tableRepository = tableRepository;
        this.timeSource = timeSource;
        this.fieldRepository = fieldRepository;
        this.pageToViewConverter = pageToViewConverter;
        this.tableToSchemaWithRulesView = tableToSchemaWithRulesView;
    }

    @Override
    public String entityName() {
        return Table.class.getSimpleName();
    }

    @Override
    public JpaRepository<Table, Long> repository() {
        return tableRepository;
    }

    public Optional<Table> findTable(final String displayName, final LocalDate referenceDate) {
        return tableRepository.findByDisplayNameAndReferenceDate(displayName, referenceDate);
    }

    public List<String> getAllTableNames() {
        return tableRepository.getPhysicalTableNames();
    }

    @Transactional
    public PagedView<SchemaView> getAllTables(final Pageable pageable) {
        Page<Table> allTables = tableRepository.findAll(pageable);
        Page<SchemaView> schemaViews = allTables.map(tableToSchemaWithRulesView);
        return pageToViewConverter.apply(schemaViews);
    }

    public List<Table> saveTables(final Iterable<Table> tables) {
        for (final Table table : tables) {
            table.setCreatedTime(timeSource.nowAsDate());
        }

        return tableRepository.saveAll(tables);
    }

    public List<Field<?>> findAllFields(final Iterable<Long> fieldIds) {
        return fieldRepository.findAllById(fieldIds);
    }

    public void updatePeriod(final Long schemaId, final SchemaPeriod schemaPeriod) {
        tableRepository.updatePeriodDates(schemaId, schemaPeriod.getStartDate(), schemaPeriod.getEndDate());
    }

    public List<Table> findAllByIdsPreFetchingFieldsAndRules(final Iterable<Long> ids) {
        return tableRepository.findAllByIdsPreFetchingFieldsAndRules(ids);
    }
}

