package com.lombardrisk.ignis.design.server.productconfig.rule.test;

import com.lombardrisk.ignis.design.field.model.Field;
import io.vavr.Tuple2;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Set;

@Getter
@Builder
public class TestedRuleResult {

    private Set<Field> contextFields;
    private List<Tuple2<String, String>> errors;
    private Boolean result;
}
