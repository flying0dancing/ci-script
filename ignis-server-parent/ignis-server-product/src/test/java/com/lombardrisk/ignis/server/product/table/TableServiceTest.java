package com.lombardrisk.ignis.server.product.table;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaPeriod;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.table.model.Table;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.lombardrisk.ignis.common.fixtures.PopulatedDates.toDate;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TableServiceTest {

    @Mock
    private TableRepository tableRepository;
    @Mock
    private TimeSource timeSource;

    @Captor
    private ArgumentCaptor<Iterable<Table>> tablesRequestCaptor;

    @InjectMocks
    private TableService tableService;

    @Test
    public void findTable_FindsTableByDisplayNameAndReferenceDate() {
        LocalDate referenceDate = LocalDate.of(2000, 1, 1);
        when(tableRepository.findByDisplayNameAndReferenceDate("employee display name", referenceDate))
                .thenReturn(Optional.of(ProductPopulated.table().build()));

        Optional<Table> optionalTable = tableService.findTable("employee display name", referenceDate);

        assertThat(optionalTable.isPresent()).isTrue();
    }

    @Test
    public void getAllTables() {
        when(tableRepository.getPhysicalTableNames())
                .thenReturn(ImmutableList.of("employee"));

        assertThat(tableService.getAllTableNames().size()).isEqualTo(1);
    }

    @Test
    public void findByIds_DelegatesToRepository() {
        tableService.findAllByIds(asList(1L, 2L, 3L));

        verify(tableRepository).findAllById(asList(1L, 2L, 3L));
    }

    @Test
    public void findByIds_ReturnsResultFromRepository() {
        Table table = ProductPopulated.table().build();

        when(tableRepository.findAllById(asList(1L, 2L, 3L)))
                .thenReturn(Collections.singletonList(table));

        List<Table> allByIds = tableService.findAllByIds(asList(1L, 2L, 3L));

        assertThat(allByIds)
                .containsExactly(table);
    }

    @Test
    public void repository_returnsTableRepository() {
        assertThat(tableService.repository())
                .isEqualTo(tableRepository);
    }

    @Test
    public void saveTables_CallsTableRepository() {
        List<Table> tables = asList(
                ProductPopulated.table().physicalTableName("first table").build(),
                ProductPopulated.table().physicalTableName("second table").build()
        );

        tableService.saveTables(tables);

        verify(tableRepository).saveAll(tables);
    }

    @Test
    public void saveTables_ReturnsSavedTables() {
        List<Table> tables = asList(
                ProductPopulated.table().physicalTableName("first table").build(),
                ProductPopulated.table().physicalTableName("second table").build()
        );

        List<Table> savedTables = asList(
                ProductPopulated.table().id(111L).physicalTableName("table 1").build(),
                ProductPopulated.table().id(222L).physicalTableName("table 2").build()
        );

        when(tableRepository.saveAll(any()))
                .thenReturn(savedTables);

        assertThat(tableService.saveTables(tables)).isSameAs(savedTables);
    }

    @Test
    public void saveTables_SavesTablesWithCreatedTime() {
        when(timeSource.nowAsDate())
                .thenReturn(toDate("2000-01-01"));

        List<Table> tables = singletonList(ProductPopulated.table().createdTime(null).build());

        tableService.saveTables(tables);

        verify(tableRepository).saveAll(tablesRequestCaptor.capture());

        Table table = tablesRequestCaptor.getValue().iterator().next();

        assertThat(table.getCreatedTime())
                .isInSameDayAs(toDate("2000-01-01"));
    }

    @Test
    public void updatePeriodDates_UpdatesPeriodDates() {
        LocalDate startDate = LocalDate.of(1999, 9, 9);
        LocalDate endDate = LocalDate.of(1999, 10, 10);

        tableService.updatePeriod(12L, SchemaPeriod.between(startDate, endDate));

        verify(tableRepository).updatePeriodDates(12L, startDate, endDate);
    }
}
