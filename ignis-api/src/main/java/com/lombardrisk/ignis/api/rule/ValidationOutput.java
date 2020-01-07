package com.lombardrisk.ignis.api.rule;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationOutput {

    public static final String DATASET_ROW_KEY = "DATASET_ROW_KEY";
    public static final String VALIDATION_RULE_ID = "VALIDATION_RULE_ID";
    public static final String DATASET_ID = "DATASET_ID";
    public static final String VALIDATION_RULE_RESULTS = "VALIDATION_RULE_RESULTS";
    public static final String TEMP_VALIDATION_RESULT = "VALIDATION_SUCCESSFUL";
    public static final String VALIDATION_RESULT_TYPE = "RESULT_TYPE";
    public static final String ERROR_MESSAGE = "ERROR_MESSAGE";

    public enum ResultType {
        SUCCESS,
        FAIL,
        ERROR
    }
}
