package com.lombardrisk.ignis.izpack.panel.validation;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator.Status;
import com.lombardrisk.ignis.izpack.InstallDataFixtures;
import com.lombardrisk.ignis.izpack.panel.validation.DatabaseValidator.ConnectionProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.SQLException;

import static com.lombardrisk.ignis.config.VariableConstants.DATABASE_APP_PASSWORD;
import static com.lombardrisk.ignis.config.VariableConstants.DATABASE_APP_USER;
import static com.lombardrisk.ignis.config.VariableConstants.DATABASE_JDBC_URL;
import static com.lombardrisk.ignis.config.VariableConstants.DATABASE_OWNER_PASSWORD;
import static com.lombardrisk.ignis.config.VariableConstants.DATABASE_OWNER_USER;
import static com.lombardrisk.ignis.config.VariableConstants.DATABASE_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseValidatorTest {

    @Mock
    private ConnectionProvider connectionProvider;

    private DatabaseValidator databaseValidator;
    private InstallData installData = InstallDataFixtures.installData();

    @Before
    public void setup() throws SQLException {
        databaseValidator = new DatabaseValidator(connectionProvider);

        when(connectionProvider.connect(anyString(), anyString(), anyString()))
                .thenReturn(mock(Connection.class));
    }

    @Test
    public void validateData_ReturnsOK() throws SQLException {
        installData.setVariable(DATABASE_JDBC_URL, "jdbc:1");
        installData.setVariable(DATABASE_OWNER_USER, "owner");
        installData.setVariable(DATABASE_OWNER_PASSWORD, "owner_pass");

        assertThat(databaseValidator.validateData(installData))
                .isEqualTo(Status.OK);

        verify(connectionProvider)
                .connect(anyString(), anyString(), anyString());
    }

    @Test
    public void validateData_OracleDatabase_ReturnsOK() throws SQLException {
        installData.setVariable(DATABASE_TYPE, "oracle");

        installData.setVariable(DATABASE_JDBC_URL, "jdbc:1");
        installData.setVariable(DATABASE_OWNER_USER, "owner");
        installData.setVariable(DATABASE_OWNER_PASSWORD, "owner_pass");

        installData.setVariable(DATABASE_APP_USER, "app");
        installData.setVariable(DATABASE_APP_PASSWORD, "app_pass");

        assertThat(databaseValidator.validateData(installData))
                .isEqualTo(Status.OK);

        verify(connectionProvider, times(2))
                .connect(anyString(), anyString(), anyString());
    }

    @Test
    public void validateData_InvalidConnectionDetails_ReturnsError() throws SQLException {
        installData.setVariable(DATABASE_JDBC_URL, "jdc:1");
        installData.setVariable(DATABASE_OWNER_USER, "owner");
        installData.setVariable(DATABASE_OWNER_PASSWORD, "owner_pass");

        when(connectionProvider.connect(any(), any(), any()))
                .thenThrow(new SQLException("Cannot find driver"));

        assertThat(databaseValidator.validateData(installData))
                .isEqualTo(Status.ERROR);
        assertThat(databaseValidator.getErrorMessageId())
                .contains(DATABASE_JDBC_URL)
                .contains("connection string").contains("jdc:1")
                .contains("username").contains("owner")
                .contains("password").contains("*****");

        verify(connectionProvider)
                .connect(anyString(), anyString(), anyString());
    }
}