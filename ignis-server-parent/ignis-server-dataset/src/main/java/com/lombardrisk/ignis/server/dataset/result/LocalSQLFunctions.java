package com.lombardrisk.ignis.server.dataset.result;

public class LocalSQLFunctions implements SQLFunctions {

    private static final long serialVersionUID = -1933114494642001454L;

    @Override
    public String toDate(final String value, final String format) {
        return String.format("TO_DATE('%s', '%s')", value, format);
    }

    @Override
    public String toTimestamp(final String value, final String format) {
        return String.format("PARSEDATETIME('%s', '%s')", value, format);
    }
}
