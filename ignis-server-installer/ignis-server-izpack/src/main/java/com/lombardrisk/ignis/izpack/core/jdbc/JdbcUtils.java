package com.lombardrisk.ignis.izpack.core.jdbc;

import lombok.experimental.UtilityClass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.trimToNull;

@UtilityClass
public final class JdbcUtils {

    public static boolean tableNotExists(final Connection connection, final String tableName) throws SQLException {
        Connection dbConnection = requireNonNull(connection, "connection cannot be null");
        String dbTableName = requireNonNull(trimToNull(tableName), "tableName cannot be blank");

        try (ResultSet tablesResultSet = dbConnection.getMetaData()
                .getTables(null, null, dbTableName, null)) {
            return !tablesResultSet.next();
        }
    }
}