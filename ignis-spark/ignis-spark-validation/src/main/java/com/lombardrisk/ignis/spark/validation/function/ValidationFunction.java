package com.lombardrisk.ignis.spark.validation.function;

import com.lombardrisk.ignis.spark.validation.transform.JexlFunction;
import com.lombardrisk.ignis.spark.validation.transform.JexlTransformation;

public class ValidationFunction extends JexlFunction<Boolean, ValidationResult> {

    private static final long serialVersionUID = -5622170928856893492L;

    public ValidationFunction(final JexlTransformation<Boolean> jexlTransformation) {
        super(jexlTransformation, ValidationResult::of);
    }
}
