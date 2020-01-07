package com.lombardrisk.ignis.functional.test.utils;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RowBuilder {

    private final List<String> fields;
    private final List<Map<String, Object>> rows = new ArrayList<>();

    public static RowBuilder fields(final String... fields) {
        return new RowBuilder(ImmutableList.copyOf(fields));
    }

    public RowBuilder row(final Object... rowData) {
        Map<String, Object> row = new HashMap<>();

        for (int i = 0; i < rowData.length; i++) {
            row.put(fields.get(i), rowData[i]);
        }

        rows.add(row);
        return this;
    }

    public List<Map<String, Object>> toRows() {
        return rows;
    }
}
