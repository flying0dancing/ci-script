package com.lombardrisk.ignis.db.migration.oracle;

import com.lombardrisk.ignis.db.migration.common.SequenceSyntax;

public class OracleSyntax implements SequenceSyntax {

    @Override
    public String createSequence(final String sequenceName, final long startWith, final long incrementBy) {
        String sequenceDDL = "CREATE SEQUENCE \"%s\" INCREMENT BY %d START WITH %d CACHE 10000 NOCYCLE";
        return String.format(sequenceDDL, sequenceName, incrementBy, startWith);
    }
}
