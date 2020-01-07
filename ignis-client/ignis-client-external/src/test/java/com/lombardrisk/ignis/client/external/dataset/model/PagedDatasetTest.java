package com.lombardrisk.ignis.client.external.dataset.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.client.core.page.response.Page;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

public class PagedDatasetTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void marshallJson_EmbeddedObjectMarshaled() throws IOException {
        String validJson = "{\n"
                + "    \"_embedded\": {\n"
                + "        \"datasetList\": [\n"
                + "            {\n"
                + "              \"id\": 10098,\n"
                + "              \"name\": \"MATT_TEST_123\",\n"
                + "              \"predicate\": \"ROW_KEY >= 43361989820416 and ROW_KEY <= 43366284787711\",\n"
                + "              \"recordsCount\": 1000,\n"
                + "              \"table\": \"TRADES_DATA_SCHEDULES\",\n"
                + "              \"validationJobId\": 1523,\n"
                + "              \"createdTime\": 100009,\n"
                + "              \"entityCode\": \"ENTITY\",\n"
                + "              \"referenceDate\": 8293871298312\n"
                + "            },\n"
                + "            {\n"
                + "              \"id\": 10024,\n"
                + "              \"name\": \"MATT_TEST_123\",\n"
                + "              \"predicate\": \"ROW_KEY >= 43044162240512 and ROW_KEY <= 43048457207807\",\n"
                + "              \"recordsCount\": 4,\n"
                + "              \"table\": \"TRADES_DATA_SCHEDULES\",\n"
                + "              \"validationJobId\": 85676,\n"
                + "              \"createdTime\": 100009,\n"
                + "              \"entityCode\": \"ENTITY\",\n"
                + "              \"referenceDate\": 8293871298312\n"
                + "            }"
                + "        ]\n"
                + "    },\n"
                + "    \"_links\": {\n"
                + "        \"self\": {\n"
                + "            \"href\": \"https://localhost:8443/fcrengine/api/external/v1/datasets?sort=id,desc\"\n"
                + "        }\n"
                + "    },\n"
                + "    \"page\": {\n"
                + "        \"size\": 2147483647,\n"
                + "        \"totalElements\": 3,\n"
                + "        \"totalPages\": 1,\n"
                + "        \"number\": 0\n"
                + "    }\n"
                + "}";

        PagedDataset pagedDataset = objectMapper.readValue(validJson, PagedDataset.class);
        assertThat(pagedDataset.getEmbedded().getData())
                .contains(
                        Dataset.builder()
                                .id(10098)
                                .name("MATT_TEST_123")
                                .predicate("ROW_KEY >= 43361989820416 and ROW_KEY <= 43366284787711")
                                .table("TRADES_DATA_SCHEDULES")
                                .validationJobId(1523L)
                                .recordsCount(1000)
                                .createdTime(new Date(100009L))
                                .entityCode("ENTITY")
                                .referenceDate(new Date(8293871298312L))
                                .build(),
                        Dataset.builder()
                                .id(10024)
                                .name("MATT_TEST_123")
                                .predicate("ROW_KEY >= 43044162240512 and ROW_KEY <= 43048457207807")
                                .table("TRADES_DATA_SCHEDULES")
                                .validationJobId(85676L)
                                .recordsCount(4)
                                .createdTime(new Date(100009L))
                                .entityCode("ENTITY")
                                .referenceDate(new Date(8293871298312L))
                                .recordsCount(4)
                                .build());
    }

    @Test
    public void validJsonWithLinks_MarshaledCorrectly() throws Exception {
        String validJson = "{\n"
                + "    \"_embedded\": {\n"
                + "        \"datasetList\": []\n"
                + "    },\n"
                + "    \"_links\": {\n"
                + "        \"self\": {\n"
                + "            \"href\": \"https://localhost:8443/fcrengine/api/external/v1/datasets?sort=id,desc\"\n"
                + "        }\n"
                + "    },\n"
                + "    \"page\": {\n"
                + "        \"size\": 2147483647,\n"
                + "        \"totalElements\": 3,\n"
                + "        \"totalPages\": 1,\n"
                + "        \"number\": 0\n"
                + "    }\n"
                + "}";

        PagedDataset pagedDataset = objectMapper.readValue(validJson, PagedDataset.class);
        assertThat(pagedDataset.getEmbedded().getData())
                .isEmpty();
    }

    @Test
    public void validJson_PagesPresent() throws Exception {
        String validJson = "{\n"
                + "    \"_embedded\": {\n"
                + "        \"datasetList\": []\n"
                + "    },\n"
                + "    \"_links\": {\n"
                + "        \"self\": {\n"
                + "            \"href\": \"https://localhost:8443/fcrengine/api/external/v1/datasets?sort=id,desc\"\n"
                + "        }\n"
                + "    },\n"
                + "    \"page\": {\n"
                + "        \"size\": 2147483647,\n"
                + "        \"totalElements\": 3,\n"
                + "        \"totalPages\": 1,\n"
                + "        \"number\": 0\n"
                + "    }\n"
                + "}";

        PagedDataset pagedDataset = objectMapper.readValue(validJson, PagedDataset.class);
        assertThat(pagedDataset.getPage())
                .isEqualTo(Page.builder()
                        .size(2147483647)
                        .number(0)
                        .totalElements(3)
                        .totalPages(1)
                        .build());
    }
}