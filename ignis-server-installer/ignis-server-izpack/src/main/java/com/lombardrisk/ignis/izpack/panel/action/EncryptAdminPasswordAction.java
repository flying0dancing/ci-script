package com.lombardrisk.ignis.izpack.panel.action;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.data.PanelAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.lombardrisk.ignis.config.VariableConstants.IGNIS_ADMIN_PASSWORD;
import static com.lombardrisk.ignis.config.VariableConstants.IGNIS_ADMIN_PASSWORD_ENCRYPTED;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
public class EncryptAdminPasswordAction implements PanelAction {

    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Override
    public void executeAction(final InstallData installData, final AbstractUIHandler abstractUIHandler) {
        String plainPassword = checkNotBlank(installData.getVariable(IGNIS_ADMIN_PASSWORD));

        installData.setVariable(
                IGNIS_ADMIN_PASSWORD_ENCRYPTED,
                PASSWORD_ENCODER.encode(plainPassword));
    }

    private static String checkNotBlank(final String plainPassword) {
        if (isBlank(plainPassword)) {
            String errorMessage = "System property [" + IGNIS_ADMIN_PASSWORD + "] cannot be blank";
            log.error(errorMessage);

            throw new InstallerException(errorMessage);
        }
        return plainPassword;
    }

    @Override
    public void initialize(final PanelActionConfiguration panelActionConfiguration) {
        // noop
    }
}