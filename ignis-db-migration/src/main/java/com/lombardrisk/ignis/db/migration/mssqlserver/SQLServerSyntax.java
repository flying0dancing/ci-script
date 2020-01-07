package com.lombardrisk.ignis.db.migration.mssqlserver;

import com.lombardrisk.ignis.db.migration.common.SequenceSyntax;

public class SQLServerSyntax implements SequenceSyntax {

    @Override
    public String createSequence(final String sequenceName, final long startWith, final long incrementBy) {
        String sequenceDDL = "CREATE SEQUENCE \"%s\" INCREMENT BY %d START WITH %d CACHE 10000 NO CYCLE";
        return String.format(sequenceDDL, sequenceName, incrementBy, startWith);
    }
}
