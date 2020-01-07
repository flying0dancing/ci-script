package com.lombardrisk.ignis.db.migration.oracle;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OracleSyntaxTest {

    private OracleSyntax syntax = new OracleSyntax();

    @Test
    public void createSequence() {
        assertThat(syntax.createSequence("my_sequence", 1L, 999L))
                .isEqualTo("CREATE SEQUENCE \"my_sequence\" INCREMENT BY 999 START WITH 1 CACHE 10000 NOCYCLE");
    }
}