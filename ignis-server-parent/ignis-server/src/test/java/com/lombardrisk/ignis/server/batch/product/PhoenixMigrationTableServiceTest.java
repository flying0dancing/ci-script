package com.lombardrisk.ignis.server.batch.product;

import com.lombardrisk.ignis.server.batch.product.datasource.PhoenixMigrationTableService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Strict.class)
public class PhoenixMigrationTableServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private PhoenixMigrationTableService phoenixMigrationTableService;

    @Before
    public void setUp() {
        phoenixMigrationTableService = new PhoenixMigrationTableService(jdbcTemplate, 3);
    }

    @Test
    public void isPhysicalSchemaMissing_SchemaIsMissing_ReturnsTrue() {
        when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                .thenReturn(emptyList());

        assertThat(
                phoenixMigrationTableService.isPhysicalSchemaMissing("A")
        ).isTrue();
    }

    @Test
    public void isPhysicalSchemaMissing_SchemaIsPresent_ReturnsFalse() {
        when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                .thenReturn(singletonList("A"));

        assertThat(
                phoenixMigrationTableService.isPhysicalSchemaMissing("A")
        ).isFalse();
    }

    @Test
    public void isPhysicalSchemaPresent_SchemaIsMissing_ReturnsFalse() {
        when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                .thenReturn(emptyList());

        assertThat(
                phoenixMigrationTableService.isPhysicalSchemaPresent("B")
        ).isFalse();
    }

    @Test
    public void isPhysicalSchemaPresent_SchemaIsPresent_ReturnsTrue() {
        when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                .thenReturn(singletonList("B"));

        assertThat(
                phoenixMigrationTableService.isPhysicalSchemaPresent("B")
        ).isTrue();
    }
}
