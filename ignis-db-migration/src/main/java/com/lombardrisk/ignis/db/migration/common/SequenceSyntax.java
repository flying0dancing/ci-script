package com.lombardrisk.ignis.db.migration.common;

public interface SequenceSyntax {

    String createSequence(String sequenceName, long startWith, long incrementBy);
}
