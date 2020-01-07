package com.lombardrisk.ignis.izpack.panel.validation;

import com.izforge.izpack.api.data.InstallData;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.stream.Stream;

import static com.lombardrisk.ignis.config.VariableConstants.DATABASE_APP_PASSWORD;
import static com.lombardrisk.ignis.config.VariableConstants.DATABASE_APP_USER;
import static com.lombardrisk.ignis.config.VariableConstants.DATABASE_JDBC_URL;
import static com.lombardrisk.ignis.config.VariableConstants.DATABASE_OWNER_PASSWORD;
import static com.lombardrisk.ignis.config.VariableConstants.DATABASE_OWNER_USER;
import static com.lombardrisk.ignis.config.VariableConstants.DATABASE_TYPE;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.trimToNull;

@Slf4j
public class DatabaseValidator extends SimpleDataValidator {

    private final ConnectionProvider connectionProvider;

    @SuppressWarnings("unused")
    public DatabaseValidator() {
        connectionProvider = new ConnectionProvider();
    }

    DatabaseValidator(final ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public Stream<Optional<String>> validate(final InstallData installData) {
        return Stream.of(
                validateOwnerUserConnection(installData),
                validateAppUserConnection(installData));
    }

    private Optional<String> validateOwnerUserConnection(final InstallData installData) {
        return validateJdbc(
                installData.getVariable(DATABASE_JDBC_URL),
                installData.getVariable(DATABASE_OWNER_USER),
                installData.getVariable(DATABASE_OWNER_PASSWORD));
    }

    private Optional<String> validateAppUserConnection(final InstallData installData) {
        if ("oracle".equalsIgnoreCase(installData.getVariable(DATABASE_TYPE))) {
            return validateJdbc(
                    installData.getVariable(DATABASE_JDBC_URL),
                    installData.getVariable(DATABASE_APP_USER),
                    installData.getVariable(DATABASE_APP_PASSWORD));
        }
        return Optional.empty();
    }

    private Optional<String> validateJdbc(final String url, final String username, final String password) {
        String error = null;

        String user = trimToNull(username);
        String pass = trimToNull(password);

        try (Connection connection = connectionProvider.connect(url, user, pass)) {
            connection.setReadOnly(true);
        } catch (SQLException e) {
            error = toConnectionErrorMessage(url, user, pass);
            log.error(error, e);
        }
        return Optional.ofNullable(error);
    }

    private static String toConnectionErrorMessage(final String url, final String username, final String password) {
        String passwordMessagePart = password != null ? "*****" : EMPTY;
        String usernameMessagePart = username != null ? username : EMPTY;

        return "[" + DATABASE_JDBC_URL + "] Could not connect to database using"
                + " connection string '" + url + "'"
                + " with username '" + usernameMessagePart + "'"
                + " and password '" + passwordMessagePart + "'";
    }

    static class ConnectionProvider {

        Connection connect(
                final String url,
                final String username,
                final String password) throws SQLException {

            DriverManager.registerDriver(new org.h2.Driver());
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
            DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
            DriverManager.registerDriver(new com.mysql.jdbc.Driver());

            return DriverManager.getConnection(url, username, password);
        }
    }
}
