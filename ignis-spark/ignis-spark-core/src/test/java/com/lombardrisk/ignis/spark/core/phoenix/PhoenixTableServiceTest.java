package com.lombardrisk.ignis.spark.core.phoenix;

import com.lombardrisk.ignis.common.lang.ErrorMessage;
import com.lombardrisk.ignis.spark.core.schema.DatasetTableSchema;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PhoenixTableServiceTest {

    @Mock
    private Connection dbConnection;

    @Mock
    private Statement statement;

    @Mock
    private DatabaseMetaData databaseMetaData;

    @Mock
    private ResultSet resultSet;

    @Mock
    private PhoenixTableService.SqlConnectionConsumer resultSetConsumer;

    private PhoenixTableService tableService;

    @Before
    public void setUp() throws Exception {
        when(dbConnection.getMetaData())
                .thenReturn(databaseMetaData);

        tableService = new PhoenixTableService(() -> dbConnection, 123, false);
    }

    @Test
    public void findTable_dbConnectionThrowsException_ReturnErrorMessage() throws SQLException {
        when(databaseMetaData.getTables(any(), any(), any(), any()))
                .thenThrow(new SQLException("WHOOPS"));

        assertThat(tableService.findTable(null).getLeft())
                .isEqualTo(ErrorMessage.of("WHOOPS"));
    }

    @Test
    public void findTable_tableNotFound_ReturnErrorMessage() throws SQLException {
        when(databaseMetaData.getTables(any(), any(), any(), any()))
                .thenReturn(resultSet);
        when(resultSet.next())
                .thenThrow(new SQLException("TABLE HAS NO NEXT"));

        assertThat(tableService.findTable(null).getLeft())
                .isEqualTo(ErrorMessage.of("Could not find table null"));
    }

    @Test
    public void findTable_dbConnectionReturnsResultSet_ReturnResultSet() throws SQLException {
        when(databaseMetaData.getTables(any(), any(), any(), any()))
                .thenReturn(resultSet);

        when(resultSet.next())
                .thenReturn(true);

        assertThat(tableService.findTable(null).right().get())
                .isEqualTo(resultSet);
    }

    @Test
    public void findTable_dbConnectionResultSetHasNoNext_ReturnErrorMessage() throws SQLException {
        when(databaseMetaData.getTables(any(), any(), any(), any()))
                .thenReturn(resultSet);

        when(resultSet.next())
                .thenReturn(false);

        Either<ErrorMessage, ResultSet> table = tableService.findTable(null);
        ErrorMessage errorMessage = table
                .left()
                .get();

        assertThat(errorMessage.getMessage())
                .isEqualTo("Could not find table null");
    }

    @Test
    public void ifTableExists_dbConnectionThrowsException_ConsumerNotCalled() {
        tableService.ifNotTableExists(null, resultSetConsumer);

        verifyZeroInteractions(resultSetConsumer);
    }

    @Test
    public void ifTableExists_dbConnectionThrowsException_ReturnsErrorMessage() throws SQLException {
        when(databaseMetaData.getTables(any(), any(), any(), any()))
                .thenThrow(new SQLException("WHOOPS"));

        assertThat(tableService.ifNotTableExists(null, resultSetConsumer).get())
                .isEqualTo(ErrorMessage.of("java.sql.SQLException: WHOOPS"));
    }

    @Test
    public void ifTableExists_tableQueryThrowsException_ConsumerCalled() throws Exception {
        when(databaseMetaData.getTables(any(), any(), any(), any()))
                .thenReturn(resultSet);
        when(resultSet.next())
                .thenThrow(new SQLException("TABLE HAS NO NEXT"));

        tableService.ifNotTableExists(null, resultSetConsumer);

        verify(resultSetConsumer).accept(dbConnection);
    }

    @Test
    public void ifTableExists_tableFound_ConsumerNotCalled() {
        tableService.ifNotTableExists(null, resultSetConsumer);

        verifyZeroInteractions(resultSetConsumer);
    }

    @Test
    public void ifTableExists_tableNotFound_ConsumerCalled() throws Exception {
        when(databaseMetaData.getTables(any(), any(), any(), any()))
                .thenReturn(resultSet);
        when(resultSet.next())
                .thenReturn(false);

        tableService.ifNotTableExists(null, resultSetConsumer);

        verify(resultSetConsumer).accept(dbConnection);
    }

    @Test
    public void createTableIfNotFound_TableNotFound_ExecutesCreateStatement() throws Exception {
        when(databaseMetaData.getTables(any(), any(), any(), any()))
                .thenReturn(resultSet);

        when(resultSet.next())
                .thenReturn(false);

        when(dbConnection.createStatement())
                .thenReturn(statement);

        StructField id = new StructField("ID", DataTypes.LongType, false, Metadata.empty());
        StructField firstName = new StructField("FIRSTNAME", DataTypes.StringType, true, Metadata.empty());
        StructField lastName = new StructField("LASTNAME", DataTypes.StringType, true, Metadata.empty());

        DatasetTableSchema schema = DatasetTableSchema.builder()
                .tableName("MY_TABLE")
                .structType(new StructType(new StructField[]{ id, firstName, lastName }))
                .primaryKeys(singletonList(id))
                .type(DatasetTableSchema.Type.FIXED)
                .build();

        tableService.createTableIfNotFound(schema);

        verify(statement).execute(
                "CREATE TABLE MY_TABLE ("
                        + "ID BIGINT NOT NULL, "
                        + "FIRSTNAME VARCHAR, "
                        + "LASTNAME VARCHAR , "
                        + "CONSTRAINT pk PRIMARY KEY (ID)"
                        + ") SALT_BUCKETS=123");

        verify(statement).close();
        verifyNoMoreInteractions(statement);
    }

    @Test
    public void createTableIfNotFound_TableFound_DoesNotExecuteCreateStatement() throws Exception {
        when(databaseMetaData.getTables(any(), any(), any(), any()))
                .thenReturn(resultSet);

        when(resultSet.next())
                .thenReturn(true);

        StructField id = new StructField("ID", DataTypes.LongType, false, Metadata.empty());
        StructField firstName = new StructField("FIRSTNAME", DataTypes.StringType, true, Metadata.empty());
        StructField lastName = new StructField("LASTNAME", DataTypes.StringType, true, Metadata.empty());

        DatasetTableSchema schema = DatasetTableSchema.builder()
                .tableName("MY_TABLE")
                .structType(new StructType(new StructField[]{ id, firstName, lastName }))
                .primaryKeys(singletonList(id))
                .type(DatasetTableSchema.Type.FIXED)
                .build();

        tableService.createTableIfNotFound(schema);

        verify(dbConnection, never()).createStatement();
        verifyZeroInteractions(statement);
    }

    @Test
    public void createTableIfNotFound_TableCreated_ReturnsNoError() throws Exception {
        when(databaseMetaData.getTables(any(), any(), any(), any()))
                .thenReturn(resultSet);

        when(resultSet.next())
                .thenReturn(false);

        when(dbConnection.createStatement())
                .thenReturn(statement);

        StructField id = new StructField("ID", DataTypes.LongType, false, Metadata.empty());
        StructField firstName = new StructField("FIRSTNAME", DataTypes.StringType, true, Metadata.empty());
        StructField lastName = new StructField("LASTNAME", DataTypes.StringType, true, Metadata.empty());

        DatasetTableSchema schema = DatasetTableSchema.builder()
                .tableName("MY_TABLE")
                .structType(new StructType(new StructField[]{ id, firstName, lastName }))
                .primaryKeys(singletonList(id))
                .type(DatasetTableSchema.Type.FIXED)
                .build();

        Option<ErrorMessage> errorMessage = tableService.createTableIfNotFound(schema);

        assertThat(errorMessage.isEmpty()).isTrue();
    }

    @Test
    public void createTableIfNotFound_FailedToLookupTable_ReturnsError() throws Exception {
        when(databaseMetaData.getTables(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("uh oh!"));

        StructField id = new StructField("ID", DataTypes.LongType, false, Metadata.empty());
        StructField firstName = new StructField("FIRSTNAME", DataTypes.StringType, true, Metadata.empty());
        StructField lastName = new StructField("LASTNAME", DataTypes.StringType, true, Metadata.empty());

        DatasetTableSchema schema = DatasetTableSchema.builder()
                .tableName("MY_TABLE")
                .structType(new StructType(new StructField[]{ id, firstName, lastName }))
                .primaryKeys(singletonList(id))
                .type(DatasetTableSchema.Type.FIXED)
                .build();

        Option<ErrorMessage> errorMessage = tableService.createTableIfNotFound(schema);

        assertThat(errorMessage.isDefined()).isTrue();
        assertThat(errorMessage.get().getMessage()).contains("uh oh!");
    }

    @Test
    public void createTableIfNotFound_FailedToLookupTable_DoesNotExecuteCreateStatement() throws Exception {
        when(databaseMetaData.getTables(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("uh oh!"));

        StructField id = new StructField("ID", DataTypes.LongType, false, Metadata.empty());
        StructField firstName = new StructField("FIRSTNAME", DataTypes.StringType, true, Metadata.empty());
        StructField lastName = new StructField("LASTNAME", DataTypes.StringType, true, Metadata.empty());

        DatasetTableSchema schema = DatasetTableSchema.builder()
                .tableName("MY_TABLE")
                .structType(new StructType(new StructField[]{ id, firstName, lastName }))
                .primaryKeys(singletonList(id))
                .type(DatasetTableSchema.Type.FIXED)
                .build();

        tableService.createTableIfNotFound(schema);

        verify(dbConnection, never()).createStatement();
        verifyZeroInteractions(statement);
    }

    @Test
    public void createTableIfNotFound_FailedToCreateTable_ReturnsError() throws Exception {
        when(databaseMetaData.getTables(any(), any(), any(), any()))
                .thenReturn(resultSet);

        when(resultSet.next())
                .thenReturn(false);

        when(dbConnection.createStatement())
                .thenReturn(statement);

        when(statement.execute(any()))
                .thenThrow(new RuntimeException("could not create table"));

        StructField id = new StructField("ID", DataTypes.LongType, false, Metadata.empty());
        StructField firstName = new StructField("FIRSTNAME", DataTypes.StringType, true, Metadata.empty());
        StructField lastName = new StructField("LASTNAME", DataTypes.StringType, true, Metadata.empty());

        DatasetTableSchema schema = DatasetTableSchema.builder()
                .tableName("MY_TABLE")
                .structType(new StructType(new StructField[]{ id, firstName, lastName }))
                .primaryKeys(singletonList(id))
                .type(DatasetTableSchema.Type.FIXED)
                .build();

        Option<ErrorMessage> errorMessage = tableService.createTableIfNotFound(schema);

        assertThat(errorMessage.isDefined()).isTrue();
        assertThat(errorMessage.get().getMessage()).contains("could not create table");
    }
}