package com.lombardrisk.ignis.server.dataset.fixture;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.fail;

public class JdbcFixture {

    private final JdbcTemplate jdbcTemplate;

    public JdbcFixture(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertData(
            final String tableName,
            final List<ColumnDefinition> columnHeaders,
            final List<List<Object>> values) {

        if (values.isEmpty()) {
            fail("Must supply values to insert into jdbc");
        }

        if (columnHeaders.size() != values.get(0).size()) {
            fail("Size of values to insert do no match column headers, [" + columnHeaders + "] != [" + values + "]");
        }

        createTable(tableName, columnHeaders);
        insertTestData(tableName, columnHeaders, values);
    }

    private void createTable(final String tableName, final List<ColumnDefinition> columnHeaders) {
        String columnsSql = columnHeaders.stream()
                .map(ColumnDefinition::createColumnSqlValue)
                .collect(Collectors.joining(" ,"));

        jdbcTemplate.execute("DROP TABLE IF EXISTS " + tableName);
        jdbcTemplate.execute("CREATE TABLE " + tableName + "(" + columnsSql + ")");
    }

    private void insertTestData(
            final String tableName,
            final List<ColumnDefinition> columnHeaders,
            final List<List<Object>> rows) {
        String columnsToInsertSql = columnHeaders.stream()
                .map(ColumnDefinition::getName)
                .map(JdbcFixture::quote)
                .collect(Collectors.joining(","));

        for (List<Object> row : rows) {
            String rowData = row.stream()
                    .map(this::toSqlData)
                    .collect(Collectors.joining(" ,"));

            jdbcTemplate.execute("INSERT INTO " + tableName + " (" + columnsToInsertSql + ") "
                    + "VALUES (" + rowData + ")");
        }
    }

    private String toSqlData(final Object o) {
        Class<?> rowType = o.getClass();

        if (rowType.isAssignableFrom(String.class)) {
            return "'" + o + "'";
        }

        if (rowType.isAssignableFrom(LocalDate.class)) {
            String dateString = DateTimeFormatter.ISO_DATE.format(LocalDate.class.cast(o));
            return "'" + dateString + "'";
        }

        return o.toString();
    }

    @AllArgsConstructor
    @Getter
    public static class ColumnDefinition {

        private final String name;
        private final String jdbcType;

        public String createColumnSqlValue() {
            return quote(name) + " " + jdbcType;
        }
    }

    private static String quote(final String name) {
        return "\"" + name + "\"";
    }
}
