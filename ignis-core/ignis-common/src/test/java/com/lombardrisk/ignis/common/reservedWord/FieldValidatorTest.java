package com.lombardrisk.ignis.common.reservedWord;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FieldValidatorTest {

    private FieldValidator fieldValidator;

    @Before
    public void setUp() {
        fieldValidator = new FieldValidator();
    }

    @Test
    public void catchReservedWords_NameStartsWithFCR_SYS_catchStringFcr_sys() {
        List<String> values = fieldValidator.catchReservedWords("FCR_SYSanyValue");
        assertThat(values).containsExactly("fcr_sys");
    }

    @Test
    public void catchReservedWords_NameWithReservedWord_returnsListWithReservedWordValue() {
        List<String> invalidWords = fieldValidator.catchReservedWords(String.format("Value with reserved word %s",
                ReservedWord.DELETE.getValue()));

        assertThat(invalidWords).containsExactlyInAnyOrder(ReservedWord.DELETE.getValue());
    }

    @Test
    public void catchReservedWords_NameStartsWithFCR_SYSAndContainsReservedWord_catchesAllInvalidWords() {
        List<String> invalidWords =
                fieldValidator.catchReservedWords(String.format("FCR_SYSanyValue %s", ReservedWord.TABLE.getValue()));
        assertThat(invalidWords).containsExactlyInAnyOrder("fcr_sys", ReservedWord.TABLE.getValue());
    }

    @Test
    public void catchReservedWords_NameWithoutAnyReservedWord_returnsEmptyList() {
        List<String> values = fieldValidator.catchReservedWords("value without any reserved word");
        assertThat(values).isEmpty();
    }
}