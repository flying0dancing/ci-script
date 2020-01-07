package com.lombardrisk.ignis.izpack.panel.validation;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator;
import com.lombardrisk.ignis.izpack.InstallDataFixtures;
import org.junit.Test;

import static com.lombardrisk.ignis.izpack.core.InstallerProps.PHOENIX_QUERY_HOST_PROP;
import static com.lombardrisk.ignis.izpack.core.InstallerProps.PHOENIX_QUERY_PORT_PROP;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;

public class PhoenixValidatorTest {

    private PhoenixValidator phoenixValidator = new PhoenixValidator();
    private InstallData installData = InstallDataFixtures.installData();

    @Test
    public void validateData_ReturnsOK() {
        installData.setVariable(PHOENIX_QUERY_HOST_PROP, "a.com");
        installData.setVariable(PHOENIX_QUERY_PORT_PROP, "8765");

        assertThat(phoenixValidator.validateData(installData))
                .isEqualTo(DataValidator.Status.OK);
    }

    @Test
    public void validateData_BlankHost_ReturnsError() {
        installData.setVariable(PHOENIX_QUERY_HOST_PROP, null);

        assertThat(phoenixValidator.validateData(installData))
                .isEqualTo(DataValidator.Status.ERROR);

        installData.setVariable(PHOENIX_QUERY_HOST_PROP, EMPTY);

        assertThat(phoenixValidator.validateData(installData))
                .isEqualTo(DataValidator.Status.ERROR);
        assertThat(phoenixValidator.getErrorMessageId())
                .contains(PHOENIX_QUERY_HOST_PROP)
                .contains("must not be blank");
    }

    @Test
    public void validateData_BlankPort_ReturnsError() {
        installData.setVariable(PHOENIX_QUERY_PORT_PROP, null);

        assertThat(phoenixValidator.validateData(installData))
                .isEqualTo(DataValidator.Status.ERROR);

        installData.setVariable(PHOENIX_QUERY_PORT_PROP, EMPTY);

        assertThat(phoenixValidator.validateData(installData))
                .isEqualTo(DataValidator.Status.ERROR);
        assertThat(phoenixValidator.getErrorMessageId())
                .contains(PHOENIX_QUERY_PORT_PROP)
                .contains("must not be blank");
    }

    @Test
    public void validateData_InvalidPort_ReturnsError() {
        installData.setVariable(PHOENIX_QUERY_PORT_PROP, "8o");

        assertThat(phoenixValidator.validateData(installData))
                .isEqualTo(DataValidator.Status.ERROR);

        installData.setVariable(PHOENIX_QUERY_PORT_PROP, "80000");

        assertThat(phoenixValidator.validateData(installData))
                .isEqualTo(DataValidator.Status.ERROR);

        installData.setVariable(PHOENIX_QUERY_PORT_PROP, "-80");

        assertThat(phoenixValidator.validateData(installData))
                .isEqualTo(DataValidator.Status.ERROR);

        assertThat(phoenixValidator.getErrorMessageId())
                .contains(PHOENIX_QUERY_PORT_PROP)
                .contains("must be a number between 0 and 65535");
    }
}