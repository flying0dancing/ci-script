package com.lombardrisk.ignis.izpack.panel.validation;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator.Status;
import com.lombardrisk.ignis.izpack.InstallDataFixtures;
import org.junit.Before;
import org.junit.Test;

import static com.lombardrisk.ignis.izpack.core.InstallerProps.SERVER_CONTEXT_PATH_PROP;
import static com.lombardrisk.ignis.izpack.core.InstallerProps.SERVER_HTTPS_PORT_PROP;
import static com.lombardrisk.ignis.izpack.core.InstallerProps.SERVER_HTTP_PORT_PROP;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;

public class ServerValidatorTest {

    private ServerValidator serverValidator = new ServerValidator();
    private InstallData installData = InstallDataFixtures.installData();

    @Before
    public void setup() {
        installData.setVariable(SERVER_HTTP_PORT_PROP, "8080");
        installData.setVariable(SERVER_HTTPS_PORT_PROP, "8443");
        installData.setVariable(SERVER_CONTEXT_PATH_PROP, "fcr");
    }

    @Test
    public void validateData_ReturnsOK() {
        installData.setVariable(SERVER_HTTP_PORT_PROP, "3450");
        installData.setVariable(SERVER_HTTPS_PORT_PROP, "6780");
        installData.setVariable(SERVER_CONTEXT_PATH_PROP, "fcrengine");

        assertThat(serverValidator.validateData(installData))
                .isEqualTo(Status.OK);
    }

    @Test
    public void validateData_BlankContextPath_ReturnsError() {
        installData.setVariable(SERVER_CONTEXT_PATH_PROP, EMPTY);

        assertThat(serverValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        installData.setVariable(SERVER_CONTEXT_PATH_PROP, null);

        assertThat(serverValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        assertThat(serverValidator.getErrorMessageId())
                .contains(SERVER_CONTEXT_PATH_PROP)
                .contains("must not be blank");
    }

    @Test
    public void validateData_BlankHttpPort_ReturnsError() {
        installData.setVariable(SERVER_HTTP_PORT_PROP, EMPTY);

        assertThat(serverValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        installData.setVariable(SERVER_HTTP_PORT_PROP, null);

        assertThat(serverValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        assertThat(serverValidator.getErrorMessageId())
                .contains(SERVER_HTTP_PORT_PROP)
                .contains("must not be blank");
    }

    @Test
    public void validateData_InvalidHttpPort_ReturnsError() {
        installData.setVariable(SERVER_HTTP_PORT_PROP, "8o");

        assertThat(serverValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        installData.setVariable(SERVER_HTTP_PORT_PROP, "80000");

        assertThat(serverValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        installData.setVariable(SERVER_HTTP_PORT_PROP, "-80");

        assertThat(serverValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        assertThat(serverValidator.getErrorMessageId())
                .contains(SERVER_HTTP_PORT_PROP)
                .contains("must be a number between 0 and 65535");
    }

    @Test
    public void validateData_BlankHttpsPort_ReturnsError() {
        installData.setVariable(SERVER_HTTPS_PORT_PROP, EMPTY);

        assertThat(serverValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        installData.setVariable(SERVER_HTTPS_PORT_PROP, null);

        assertThat(serverValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        assertThat(serverValidator.getErrorMessageId())
                .contains(SERVER_HTTPS_PORT_PROP)
                .contains("must not be blank");
    }

    @Test
    public void validateData_InvalidHttpsPort_ReturnsError() {
        installData.setVariable(SERVER_HTTPS_PORT_PROP, "8o");

        assertThat(serverValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        installData.setVariable(SERVER_HTTPS_PORT_PROP, "65536");

        assertThat(serverValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        installData.setVariable(SERVER_HTTPS_PORT_PROP, "-1");

        assertThat(serverValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        assertThat(serverValidator.getErrorMessageId())
                .contains(SERVER_HTTPS_PORT_PROP)
                .contains("must be a number between 0 and 65535");
    }
}