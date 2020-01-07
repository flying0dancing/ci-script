package com.lombardrisk.ignis.spark.staging.datafields;

import lombok.Data;

import java.io.Serializable;

@Data
public class IndexedDataField implements Serializable {

    private static final long serialVersionUID = 5202596433304944579L;

    private final String value;
    private final Integer index;

    public IndexedDataField(final int index, final String value) {
        this.value = value;
        this.index = index;
    }
}
