package com.lombardrisk.ignis.client.external.dataset;

import com.lombardrisk.ignis.client.core.fixture.ClientCore;
import com.lombardrisk.ignis.client.core.page.request.Sort;
import com.lombardrisk.ignis.client.external.fixture.ExternalClient;
import org.junit.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class DatasetQueryParamsTest {

    @Test
    public void toParameterMap_EmptyQuery_ReturnsEmptyMap() {
        DatasetQueryParams query = DatasetQueryParams.builder().build();

        assertThat(query.toParameterMap())
                .isEmpty();
    }

    @Test
    public void toParameterMap_PagedQuery_ReturnsPagedParams() {
        DatasetQueryParams query = ExternalClient.Populated.datasetQueryParams()
                .pageRequest(ClientCore.Populated.pageRequest()
                        .size(100)
                        .page(2)
                        .build())
                .build();

        assertThat(query.toParameterMap())
                .contains(
                        entry("page", 2),
                        entry("size", 100)
                );
    }

    @Test
    public void toParameterMap_SortedQuery_ReturnsSortParams() {
        DatasetQueryParams query = ExternalClient.Populated.datasetQueryParams()
                .pageRequest(ClientCore.Populated.pageRequest()
                        .sort(Sort.builder()
                                .field("name")
                                .direction(Sort.Direction.ASC)
                                .build())
                        .build())

                .build();

        assertThat(query.toParameterMap())
                .contains(
                        entry("sort", "name,asc"));
    }

    @Test
    public void toParameterMap_Name_ReturnsNameParam() {
        DatasetQueryParams query = ExternalClient.Populated.datasetQueryParams()
                .name("name")
                .build();

        assertThat(query.toParameterMap())
                .contains(
                        entry("datasetName", "name"));
    }

    @Test
    public void toParameterMap_Schema_ReturnsSchemaParam() {
        DatasetQueryParams query = ExternalClient.Populated.datasetQueryParams()
                .schema("schema")
                .build();

        assertThat(query.toParameterMap())
                .contains(
                        entry("datasetSchema", "schema"));
    }

    @Test
    public void toParameterMap_EntityCode_ReturnsEntityCodeParam() {
        DatasetQueryParams query = ExternalClient.Populated.datasetQueryParams()
                .entityCode("entityCode123")
                .build();

        assertThat(query.toParameterMap())
                .contains(
                        entry("entityCode", "entityCode123"));
    }

    @Test
    public void toParameterMap_ReferenceDate_ReturnsReferenceDateParam() {
        LocalDate referenceDate = LocalDate.of(2000, 1, 1);

        DatasetQueryParams query = ExternalClient.Populated.datasetQueryParams()
                .referenceDate(referenceDate)
                .build();

        assertThat(query.toParameterMap())
                .contains(
                        entry("referenceDate", "2000-01-01"));
    }
}