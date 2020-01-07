package com.lombardrisk.ignis.client.core.view;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lombardrisk.ignis.client.core.page.response.Page;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
@JsonIgnoreProperties(ignoreUnknown = true)
public class PagedView<T> {

    private final List<T> data;
    private final Page page;
}
