package com.lombardrisk.ignis.client.external.dataset.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lombardrisk.ignis.client.core.page.response.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PagedDataset {

    @JsonProperty("_embedded")
    private Embedded embedded;

    private Page page;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Embedded {

        @JsonProperty("datasetList")
        private List<Dataset> data;
    }

    @JsonIgnore
    public List<Dataset> datasets() {
        return Optional.ofNullable(embedded)
                .flatMap(embeddedData -> Optional.ofNullable(embeddedData.getData()))
                .orElse(Collections.emptyList());
    }
}
