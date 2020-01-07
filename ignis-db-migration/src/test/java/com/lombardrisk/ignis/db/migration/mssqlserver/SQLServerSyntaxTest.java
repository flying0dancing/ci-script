package com.lombardrisk.ignis.db.migration.mssqlserver;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SQLServerSyntaxTest {

    private SQLServerSyntax syntax = new SQLServerSyntax();

    @Test
    public void createSequence() {
        assertThat(syntax.createSequence("my_sequence", 1L, 999L))
                .isEqualTo("CREATE SEQUENCE \"my_sequence\" INCREMENT BY 999 START WITH 1 CACHE 10000 NO CYCLE");
    }
}