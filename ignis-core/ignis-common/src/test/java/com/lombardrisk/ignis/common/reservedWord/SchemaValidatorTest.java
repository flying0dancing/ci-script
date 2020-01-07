package com.lombardrisk.ignis.common.reservedWord;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SchemaValidatorTest {

    private SchemaValidator schemaValidator;

    @Before
    public void setUp() {
        schemaValidator = new SchemaValidator();
    }

    @Test
    public void catchReservedWords_NameStartsWithFCR_SYS_catchStringFcr_sys() {
        List<String> values = schemaValidator.catchReservedWords("validation_RULE_results");
        assertThat(values).containsExactly("validation_rule_results");
    }

    @Test
    public void catchReservedWords_NameWithReservedWord_returnsListWithReservedWordValue() {
        List<String> invalidWords = schemaValidator.catchReservedWords(String.format("Value with reserved word %s",
                ReservedWord.DELETE.getValue()));

        assertThat(invalidWords).containsExactlyInAnyOrder(ReservedWord.DELETE.getValue());
    }

    @Test
    public void catchReservedWords_NameWithoutAnyReservedWord_returnsEmptyList() {
        List<String> values = schemaValidator.catchReservedWords("value without any reserved word");
        assertThat(values).isEmpty();
    }
}