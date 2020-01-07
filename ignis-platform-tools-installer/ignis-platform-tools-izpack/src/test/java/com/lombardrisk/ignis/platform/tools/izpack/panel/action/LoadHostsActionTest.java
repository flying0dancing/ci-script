package com.lombardrisk.ignis.platform.tools.izpack.panel.action;

import com.izforge.izpack.api.data.InstallData;
import com.lombardrisk.ignis.platform.tools.izpack.InstallDataFixtures;
import org.junit.Test;

import static com.lombardrisk.ignis.config.VariableConstants.HOSTS;
import static com.lombardrisk.ignis.config.VariableConstants.HOSTS_COUNT;
import static org.assertj.core.api.Assertions.assertThat;

public class LoadHostsActionTest {

    private InstallData installData = InstallDataFixtures.installData();
    private LoadHostsAction loadHostsAction = new LoadHostsAction();

    @Test
    public void executeAction_SetsHostCount() {
        installData.setVariable(HOSTS, "a.co,a.co,a.co,b.b,d,e.co");
        loadHostsAction.executeAction(installData, null);

        assertThat(installData.getVariable(HOSTS_COUNT))
                .isEqualTo("4");
    }

    @Test
    public void executeAction_SetsPrefixedHosts() {
        installData.setVariable(HOSTS, "a.co,b.co");
        loadHostsAction.executeAction(installData, null);

        assertThat(installData.getVariable("hosts.1"))
                .isEqualTo("a.co");
        assertThat(installData.getVariable("hosts.2"))
                .isEqualTo("b.co");
    }
}