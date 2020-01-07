package com.lombardrisk.ignis.functional.test.assertions.expected;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ExpectedResultDetailsData {
    private final List<String> headers;
    private final List<Map<String, Object>> data;

    private ExpectedResultDetailsData(final List<String> headers, final List<Map<String, Object>> data) {
        this.headers = headers;
        this.data = data;
    }

    public static Builder expectedData() {
        return new Builder();
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public static class Builder {
        private List<String> headers;
        private List<Object[]> rows;

        public Builder headers(final String... headers) {
            this.headers = ImmutableList.copyOf(headers);
            this.rows = new ArrayList<>();
            return this;
        }

        public Builder row(final Object... row) {
            this.rows.add(row);
            return this;
        }

        public ExpectedResultDetailsData build() {
            List<Map<String, Object>> data = new ArrayList<>();

            for (Object[] row : rows) {
                Map<String, Object> values = new HashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    values.put(headers.get(i), row[i]);
                }
                data.add(values);
            }

            return new ExpectedResultDetailsData(headers, data);
        }

        public ExpectedResultDetailsData build(final List<Map<String, Object>> data) {
            return new ExpectedResultDetailsData(headers, data);
        }
    }
}
