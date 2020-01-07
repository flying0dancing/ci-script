package com.lombardrisk.ignis.platform.tools.izpack.panel.validation;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator;
import com.lombardrisk.ignis.platform.tools.izpack.InstallDataFixtures;
import org.junit.Test;

import static com.lombardrisk.ignis.config.VariableConstants.DISTRIBUTED;
import static com.lombardrisk.ignis.config.VariableConstants.HOSTS;
import static com.lombardrisk.ignis.config.VariableConstants.HOSTS_COUNT;
import static org.assertj.core.api.Assertions.assertThat;

public class HostsValidatorTest {

    private InstallData installData = InstallDataFixtures.installData();
    private HostsValidator hostsValidator = new HostsValidator();

    @Test
    public void validateData_DistributedMode_ReturnsOk() {
        installData.setVariable(DISTRIBUTED, "true");
        installData.setVariable(HOSTS, "a.com");
        installData.setVariable(HOSTS_COUNT, "3");

        assertThat(hostsValidator.validateData(installData))
                .isEqualTo(DataValidator.Status.OK);
    }

    @Test
    public void validateData_StandaloneMode_ReturnsOk() {
        installData.setVariable(DISTRIBUTED, "false");
        assertThat(hostsValidator.validateData(installData))
                .isEqualTo(DataValidator.Status.OK);
    }

    @Test
    public void validateData_LessThan3Count_ReturnsError() {
        installData.setVariable(DISTRIBUTED, "true");
        installData.setVariable(HOSTS_COUNT, "2");

        assertThat(hostsValidator.validateData(installData))
                .isEqualTo(DataValidator.Status.ERROR);
    }

    @Test
    public void validateData_BlankHosts_ReturnsError() {
        installData.setVariable(DISTRIBUTED, "true");
        installData.setVariable(HOSTS, "b.com");
        installData.setVariable(HOSTS_COUNT, "2");

        assertThat(hostsValidator.validateData(installData))
                .isEqualTo(DataValidator.Status.ERROR);
    }
}