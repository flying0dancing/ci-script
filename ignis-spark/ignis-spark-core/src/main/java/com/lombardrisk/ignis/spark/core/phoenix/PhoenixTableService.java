package com.lombardrisk.ignis.spark.core.phoenix;

import com.lombardrisk.ignis.common.lang.ErrorMessage;
import com.lombardrisk.ignis.spark.core.schema.DatasetTableSchema;
import io.vavr.Tuple0;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Function;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Slf4j
public class PhoenixTableService {

    private static final String TABLE_NOT_FOUND = "Could not find table ";

    private final SqlConnectionSupplier dbConnection;
    private final int saltBucketSize;
    private final boolean debugMode;

    public PhoenixTableService(
            final SqlConnectionSupplier dbConnection,
            final int saltBucketSize,
            final boolean debugMode) {
        this.dbConnection = dbConnection;
        this.saltBucketSize = saltBucketSize;
        this.debugMode = debugMode;
    }

    public Option<ErrorMessage> createTableIfNotFound(final DatasetTableSchema tableSchema) {
        return ifNotTableExists(tableSchema.getTableName(), connection -> createTable(tableSchema, connection));
    }

    Either<ErrorMessage, ResultSet> findTable(final String tableName) {
        Either<String, Either<String, ResultSet>> findTableResult = Try.of(() -> findTableForConnection(tableName))
                .onFailure(throwable -> log.error(TABLE_NOT_FOUND + tableName, throwable))
                .toEither()
                .mapLeft(Throwable::getMessage);

        return findTableResult
                .flatMap(Function.identity())
                .mapLeft(ErrorMessage::of);
    }

    Option<ErrorMessage> ifNotTableExists(final String tableName, final SqlConnectionConsumer consumer) {
        Either<ErrorMessage, Tuple0> phoenixUpdateResult = Try.of(() -> applyIfTableNotFound(tableName, consumer))
                .onFailure(error -> log.error("Error occurred updating Phoenix", tableName, error))
                .toEither()
                .mapLeft(ErrorMessage::of);

        return phoenixUpdateResult.left()
                .toOption();
    }

    private void createTable(final DatasetTableSchema tableSchema, final Connection connection) throws SQLException {
        log.debug("Creating Phoenix table {}", tableSchema.getTableName());

        try (Statement statement = connection.createStatement()) {
            String createTableSql = tableSchema.createTableDDL(saltBucketSize);
            if (debugMode) {
                createTableSql = createTableSql.replaceAll("\\s+SALT_BUCKETS=\\d+", EMPTY);
            }
            statement.execute(createTableSql);
        }
    }

    private Either<String, ResultSet> findTableForConnection(final String tableName) throws SQLException {
        try (Connection connection = dbConnection.get()) {

            Option<ResultSet> resultSet = lookupTable(tableName, connection);
            if (resultSet.isDefined()) {
                return Either.right(resultSet.get());
            }
            return Either.left(TABLE_NOT_FOUND + tableName);
        }
    }

    private Tuple0 applyIfTableNotFound(
            final String tableName,
            final SqlConnectionConsumer connectionConsumer) throws SQLException {

        try (Connection connection = dbConnection.get()) {

            Option<ResultSet> resultSet = lookupTable(tableName, connection);
            if (!resultSet.isDefined()) {
                connectionConsumer.accept(connection);
            }
            return Tuple0.instance();
        }
    }

    private Option<ResultSet> lookupTable(final String tableName, final Connection connection) throws SQLException {
        ResultSet tables = connection.getMetaData()
                .getTables(null, null, tableName, null);

        return Try.of(tables::next)
                .onFailure(error -> log.error("Could not find next value in returned tables {}", tables, error))
                .onSuccess(success -> log.debug("Found phoenix table {}", tableName))
                .toEither()
                .fold(
                        error -> Option.none(),
                        hasNext -> hasNext ? Option.of(tables) : Option.none());
    }

    public interface SqlConnectionSupplier {

        Connection get() throws SQLException;
    }

    public interface SqlConnectionConsumer {

        void accept(final Connection connection) throws SQLException;
    }
}
