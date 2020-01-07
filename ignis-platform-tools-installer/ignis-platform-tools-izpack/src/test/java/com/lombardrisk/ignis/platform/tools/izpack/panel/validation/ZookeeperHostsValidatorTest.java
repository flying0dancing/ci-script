package com.lombardrisk.ignis.platform.tools.izpack.panel.validation;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator.Status;
import com.lombardrisk.ignis.platform.tools.izpack.InstallDataFixtures;
import org.junit.Test;

import static com.lombardrisk.ignis.config.VariableConstants.DISTRIBUTED;
import static com.lombardrisk.ignis.config.VariableConstants.ZOOKEEPER_HOSTS;
import static org.assertj.core.api.Assertions.assertThat;

public class ZookeeperHostsValidatorTest {

    private InstallData installData = InstallDataFixtures.installData();
    private ZookeeperHostsValidator zookeeperHostsValidator = new ZookeeperHostsValidator();

    @Test
    public void validateData_DistributedMode_ReturnsOk() {
        installData.setVariable(DISTRIBUTED, "true");
        installData.setVariable(ZOOKEEPER_HOSTS, "a,b,c");

        assertThat(zookeeperHostsValidator.validateData(installData))
                .isEqualTo(Status.OK);
    }

    @Test
    public void validateData_StandaloneMode_ReturnsOk() {
        installData.setVariable(DISTRIBUTED, "false");
        installData.setVariable(ZOOKEEPER_HOSTS, "a,b");

        assertThat(zookeeperHostsValidator.validateData(installData))
                .isEqualTo(Status.OK);
    }

    @Test
    public void validateData_OddHostCount_ReturnsError() {
        installData.setVariable(DISTRIBUTED, "true");
        installData.setVariable(ZOOKEEPER_HOSTS, "a,b,c,d");

        assertThat(zookeeperHostsValidator.validateData(installData))
                .isEqualTo(Status.ERROR);
    }
}