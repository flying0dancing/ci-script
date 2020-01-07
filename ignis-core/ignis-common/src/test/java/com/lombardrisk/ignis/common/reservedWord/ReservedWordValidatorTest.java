package com.lombardrisk.ignis.common.reservedWord;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ReservedWordValidatorTest {

    private ReservedWordValidator reservedWordValidator;

    @Before
    public void setUp() {
        reservedWordValidator = new ReservedWordValidator();
    }

    @Test
    public void catchReservedWords_valueWithoutReservedWords_returnsEmptyList() {
        List<String> reservedWords = reservedWordValidator.catchReservedWords("NameValue without reserved words");

        assertThat(reservedWords).isEmpty();
    }

    @Test
    public void catchReservedWords_valueWithTableReservedWord_returnListWithTableValue() {
        List<String> reservedWords = reservedWordValidator.catchReservedWords(
                String.format(
                        "NameValue with reserved word %s",
                        ReservedWord.TABLE.getValue()));

        assertThat(reservedWords).containsExactly(ReservedWord.TABLE.getValue());
    }

    @Test
    public void catchReservedWords_catchAllSchemaReserverdWord() {
        List<String> values = Arrays.stream(ReservedWord.values())
                .map(s -> s.getValue())
                .collect(Collectors.toList());

        List<String> reservedWords = reservedWordValidator.catchReservedWords(String.join(" ", values));

        assertThat(reservedWords).containsExactlyInAnyOrder(
                ReservedWord.SELECT.getValue(),
                ReservedWord.UPSERT.getValue(),
                ReservedWord.VALUES.getValue(),
                ReservedWord.DELETE.getValue(),
                ReservedWord.DECLARE.getValue(),
                ReservedWord.CURSOR.getValue(),
                ReservedWord.OPEN.getValue(),
                ReservedWord.FETCH.getValue(),
                ReservedWord.NEXT.getValue(),
                ReservedWord.CLOSE.getValue(),
                ReservedWord.CREATE.getValue(),
                ReservedWord.TABLE.getValue(),
                ReservedWord.DROP.getValue(),
                ReservedWord.FUNCTION.getValue(),
                ReservedWord.VIEW.getValue(),
                ReservedWord.SEQUENCE.getValue(),
                ReservedWord.ALTER.getValue(),
                ReservedWord.INDEX.getValue(),
                ReservedWord.EXPLAIN.getValue(),
                ReservedWord.UPDATE.getValue(),
                ReservedWord.STATISTICS.getValue(),
                ReservedWord.SCHEMA.getValue(),
                ReservedWord.USE.getValue(),
                ReservedWord.GRANT.getValue(),
                ReservedWord.REVOKE.getValue(),
                ReservedWord.SYS.getValue(),
                ReservedWord.ROW.getValue(),
                ReservedWord.KEY.getValue(),
                ReservedWord.ROW_KEY.getValue());
    }
}