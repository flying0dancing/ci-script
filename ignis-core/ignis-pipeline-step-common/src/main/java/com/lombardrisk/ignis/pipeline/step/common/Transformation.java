package com.lombardrisk.ignis.pipeline.step.common;

import com.lombardrisk.ignis.common.stream.CollectorUtils;
import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import io.vavr.control.Validation;

import java.util.List;
import java.util.Set;

public interface Transformation {

    String toSparkSql();

    default Validation<String, String> selectString(final SelectColumn select) {
        return Validation.valid(select.getAs() == null
                ? select.getSelect()
                : select.getSelect() + " AS " + select.getAs());
    }

    default Validation<List<String>, List<String>> selectStrings(final Set<SelectColumn> selects) {
        return selects.stream()
                .map(this::selectString)
                .collect(CollectorUtils.groupValidations());
    }
}
