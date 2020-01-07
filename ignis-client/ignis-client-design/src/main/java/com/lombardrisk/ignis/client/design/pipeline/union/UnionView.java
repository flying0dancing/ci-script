package com.lombardrisk.ignis.client.design.pipeline.union;

import com.lombardrisk.ignis.client.design.pipeline.select.SelectView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class UnionView {

    private final List<SelectView> selects;
    private final List<String> filters;
}
