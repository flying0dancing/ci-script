package com.lombardrisk.ignis.izpack.panel.action;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.data.PanelAction;
import com.lombardrisk.ignis.config.VariableConstants;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.lombardrisk.ignis.izpack.core.jdbc.JdbcUtils.tableNotExists;

@Slf4j
public class CheckAdminUserExistsAction implements PanelAction {

    private static final String VAR_ADMIN_EXISTS = "admin.exists";

    @Override
    public void executeAction(final InstallData installData, final AbstractUIHandler abstractUIHandler) {
        try {
            boolean adminExists = checkAdminExists(installData);
            log.debug("User 'admin' {}", adminExists ? "exists" : "does not exist");

            installData.setVariable(VAR_ADMIN_EXISTS, String.valueOf(adminExists));
        } catch (SQLException e) {
            log.error("Unable to connect to database: \n\n" + e.getLocalizedMessage(), e);
        }
    }

    private boolean checkAdminExists(final InstallData installData) throws SQLException {
        String jdbcUrl = installData.getVariable(VariableConstants.DATABASE_JDBC_URL);
        String user = installData.getVariable(VariableConstants.DATABASE_OWNER_USER);
        String password = installData.getVariable(VariableConstants.DATABASE_OWNER_PASSWORD);

        Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
        if (tableNotExists(conn, "SEC_USER")) {
            return false;
        }

        try (
                PreparedStatement stat = conn.prepareStatement("select count(*) from SEC_USER where username='admin'");
                ResultSet rs = stat.executeQuery()) {
            while (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    @Override
    public void initialize(final PanelActionConfiguration panelActionConfiguration) {
        // noop
    }
}