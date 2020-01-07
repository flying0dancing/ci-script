package com.lombardrisk.ignis.pipeline.step.api;

public enum DatasetFieldName {

    ROW_KEY("ROW_KEY");

    private String name;

    public String getName() {
        return name;
    }

    DatasetFieldName(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
