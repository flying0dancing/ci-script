package com.lombardrisk.ignis.common.reservedWord;

import java.util.List;

public class SchemaValidator extends ReservedWordValidator {

    private static final String VALIDATION_RULE_RESULTS = "validation_rule_results";

    public List<String> catchReservedWords(final String name) {
        List<String> capturedKeyWords = super.catchReservedWords(name);

        if (capturedKeyWords.isEmpty() &&
                name.toLowerCase().equals(VALIDATION_RULE_RESULTS)) {
            capturedKeyWords.add(VALIDATION_RULE_RESULTS);
        }
        return capturedKeyWords;
    }
}
