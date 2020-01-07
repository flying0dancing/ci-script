package com.lombardrisk.ignis.platform.tools.izpack.panel.action;

import com.izforge.izpack.api.data.InstallData;
import com.lombardrisk.ignis.platform.tools.izpack.InstallDataFixtures;
import org.junit.Test;

import java.net.InetAddress;

import static com.lombardrisk.ignis.config.VariableConstants.DISTRIBUTED;
import static com.lombardrisk.ignis.config.VariableConstants.ZOOKEEPER_HOSTS;
import static com.lombardrisk.ignis.config.VariableConstants.ZOOKEEPER_MYID;
import static org.assertj.core.api.Assertions.assertThat;

public class ConfigZooKeeperActionTest {

    private InstallData installData = InstallDataFixtures.installData();

    @Test
    public void executionAction_LocalIpIsFirstInList_SetsMyIdToOne() throws Exception {
        InetAddress ip = InetAddress.getLocalHost();

        installData.setVariable(DISTRIBUTED, "true");
        installData.setVariable(ZOOKEEPER_HOSTS, ip.getHostName() + ",10.0.0.1,8.0.0.1");

        ConfigZooKeeperAction zooKeeperAction = new ConfigZooKeeperAction();
        zooKeeperAction.executeAction(installData, null);

        assertThat(installData.getVariable(ZOOKEEPER_MYID))
                .isEqualTo("1");
    }

    @Test
    public void executionAction_LocalIpIsSecondInList_SetsMyIdToTwo() throws Exception {
        InetAddress ip = InetAddress.getLocalHost();

        installData.setVariable(DISTRIBUTED, "true");
        installData.setVariable(ZOOKEEPER_HOSTS, "10.0.0.1," + ip.getHostName() + ",8.0.0.1");

        ConfigZooKeeperAction zooKeeperAction = new ConfigZooKeeperAction();
        zooKeeperAction.executeAction(installData, null);

        assertThat(installData.getVariable(ZOOKEEPER_MYID))
                .isEqualTo("2");
    }

    @Test
    public void executionAction_LocalIpIsThirdInList_SetsMyIdToThree() throws Exception {
        InetAddress ip = InetAddress.getLocalHost();

        installData.setVariable(DISTRIBUTED, "true");
        installData.setVariable(ZOOKEEPER_HOSTS, "10.0.0.1,8.0.0.1," + ip.getHostName());

        ConfigZooKeeperAction zooKeeperAction = new ConfigZooKeeperAction();
        zooKeeperAction.executeAction(installData, null);

        assertThat(installData.getVariable(ZOOKEEPER_MYID))
                .isEqualTo("3");
    }
}