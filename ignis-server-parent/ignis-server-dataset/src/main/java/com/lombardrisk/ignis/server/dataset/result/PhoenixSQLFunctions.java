package com.lombardrisk.ignis.server.dataset.result;

public class PhoenixSQLFunctions implements SQLFunctions {

    private static final long serialVersionUID = 1505797150955536267L;

    @Override
    public String toDate(final String value, final String format) {
        return String.format("TO_DATE('%s', '%s')", value, format);
    }

    @Override
    public String toTimestamp(final String value, final String format) {
        return String.format("TO_TIMESTAMP('%s', '%s')", value, format);
    }
}
