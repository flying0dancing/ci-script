package com.lombardrisk.ignis.common.reservedWord;

public enum ReservedWord {
    SELECT("select"),
    UPSERT("upsert"),
    VALUES("values"),
    DELETE("delete"),
    DECLARE("declare"),
    CURSOR("cursor"),
    OPEN("open"),
    FETCH("fetch"),
    NEXT("next"),
    CLOSE("close"),
    CREATE("create"),
    TABLE("table"),
    DROP("drop"),
    FUNCTION("function"),
    VIEW("view"),
    SEQUENCE("sequence"),
    ALTER("alter"),
    INDEX("index"),
    EXPLAIN("explain"),
    UPDATE("update"),
    STATISTICS("statistics"),
    SCHEMA("schema"),
    USE("use"),
    GRANT("grant"),
    REVOKE("revoke"),
    SYS("sys"),
    ROW("row"),
    KEY("key"),
    ROW_KEY("row_key");

    private final String value;

    ReservedWord(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
