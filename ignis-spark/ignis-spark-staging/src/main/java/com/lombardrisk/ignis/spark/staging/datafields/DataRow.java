package com.lombardrisk.ignis.spark.staging.datafields;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DataRow implements Serializable {

    private static final long serialVersionUID = 1169316169495116692L;

    private List<IndexedDataField> fields;

    public DataRow(final List<IndexedDataField> fields) {
        this.fields = fields;
    }

    public IndexedDataField getField(final int columnIndex) {
        return fields.get(columnIndex);
    }

    public int size() {
        return fields.size();
    }
}
