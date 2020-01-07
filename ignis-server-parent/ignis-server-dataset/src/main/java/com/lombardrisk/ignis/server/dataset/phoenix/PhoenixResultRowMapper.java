package com.lombardrisk.ignis.server.dataset.phoenix;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.server.product.table.model.Field;
import lombok.Builder;
import lombok.Data;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhoenixResultRowMapper implements RowMapper<Map<String, Object>> {

    private final List<QueryColumn> columns;

    public PhoenixResultRowMapper(final List<QueryColumn> columns) {
        this.columns = ImmutableList.copyOf(columns);
    }

    public static PhoenixResultRowMapper fromFields(final List<Field> fields) {
        return new PhoenixResultRowMapper(MapperUtils.map(fields, QueryColumn::fromField));
    }

    @Override
    public Map<String, Object> mapRow(@Nullable final ResultSet resultSet, final int i) throws SQLException {
        if (resultSet == null) {
            return ImmutableMap.of();
        }

        Map<String, Object> result = new HashMap<>();

        for (final QueryColumn column : columns) {
            String quotesRemoved = column.getName().replace("\"", "");
            Object columnValue = resultSet.getObject(quotesRemoved);
            result.put(quotesRemoved, columnValue);
        }

        return result;
    }

    @Builder
    @Data
    public static class QueryColumn {

        private final String expression;
        private final String name;

        public static QueryColumn fromField(final Field field) {
            String quotedFieldName = "\"" + field.getName() + "\"";
            return new QueryColumn(quotedFieldName, quotedFieldName);
        }

        public String toSelectExpression() {
            return expression + " AS " + name;
        }
    }
}
