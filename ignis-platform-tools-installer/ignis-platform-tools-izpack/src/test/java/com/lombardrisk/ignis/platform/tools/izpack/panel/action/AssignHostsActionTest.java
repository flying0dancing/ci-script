package com.lombardrisk.ignis.platform.tools.izpack.panel.action;

import com.izforge.izpack.api.data.InstallData;
import com.lombardrisk.ignis.platform.tools.izpack.InstallDataFixtures;
import org.junit.Test;

import java.net.InetAddress;

import static com.lombardrisk.ignis.config.VariableConstants.DISTRIBUTED;
import static com.lombardrisk.ignis.config.VariableConstants.HDFS_NN_HOST;
import static com.lombardrisk.ignis.config.VariableConstants.HOSTS;
import static com.lombardrisk.ignis.config.VariableConstants.HOSTS_COUNT;
import static com.lombardrisk.ignis.config.VariableConstants.HOSTS_PREFIX;
import static com.lombardrisk.ignis.config.VariableConstants.PHOENIX_QS_LOAD_BALANCER;
import static com.lombardrisk.ignis.config.VariableConstants.PHOENIX_SALT_BUCKETS;
import static java.net.InetAddress.getLocalHost;
import static org.assertj.core.api.Assertions.assertThat;

public class AssignHostsActionTest {

    private AssignHostsAction assignHostsAction = new AssignHostsAction();
    private InstallData installData = InstallDataFixtures.installData();

    @Test
    public void executeAction_DistributedMode_SetsClusterHosts() {
        installData.setVariable(DISTRIBUTED, "true");
        installData.setVariable(HOSTS, "a,b,c,d");
        installData.setVariable(HOSTS_PREFIX + "1", "a");
        installData.setVariable(HOSTS_PREFIX + "2", "b");
        installData.setVariable(HOSTS_PREFIX + "3", "c");
        installData.setVariable(HOSTS_PREFIX + "4", "d");
        installData.setVariable(HOSTS_COUNT, "4");

        assignHostsAction.executeAction(installData, null);

        assertThat(installData.getVariable(HDFS_NN_HOST))
                .isEqualTo("a");
    }

    @Test
    public void executeAction_Standalone_AddsHostsVariablesFromHostName() throws Exception {
        installData.setVariable(DISTRIBUTED, "false");

        assignHostsAction.executeAction(installData, null);

        assertThat(installData.getVariable(HDFS_NN_HOST))
                .isEqualTo(getLocalHost().getHostName());
    }

    @Test
    public void executeAction_Clustered_SelectsANodeFromHosts() {
        installData.setVariable(DISTRIBUTED, "true");
        installData.setVariable(HOSTS_COUNT, "3");
        installData.setVariable("hosts", "node1,node2,node3");
        installData.setVariable("hosts.1", "node1");
        installData.setVariable("hosts.2", "node2");
        installData.setVariable("hosts.3", "node3");

        assignHostsAction.executeAction(installData, null);

        assertThat(installData.getVariable(PHOENIX_QS_LOAD_BALANCER))
                .startsWith("node");
    }

    @Test
    public void executeAction_Standalone_SetsLocalHostAsLoadBalancer() throws Exception {
        installData.setVariable(DISTRIBUTED, "false");

        assignHostsAction.executeAction(installData, null);

        assertThat(installData.getVariable(PHOENIX_QS_LOAD_BALANCER))
                .isEqualTo(getLocalHost().getHostName());
    }

    @Test
    public void executeAction_HdfsRegionHosts_SetsPhoenixSaltBucketCount() {
        installData.setVariable(DISTRIBUTED, "true");
        installData.setVariable(HOSTS_COUNT, "4");
        installData.setVariable(HOSTS, "a,b,c,d");
        installData.setVariable(HOSTS_PREFIX + "1", "a");
        installData.setVariable(HOSTS_PREFIX + "2", "b");
        installData.setVariable(HOSTS_PREFIX + "3", "c");
        installData.setVariable(HOSTS_PREFIX + "4", "d");

        assignHostsAction.executeAction(installData, null);

        assertThat(installData.getVariable(PHOENIX_SALT_BUCKETS))
                .isEqualTo("4");
    }

    @Test
    public void executeAction_WithDistributed_SetsHosts() {
        installData.setVariable("hosts", "node1,node2,node3");
        installData.setVariable("distributed", "true");
        installData.setVariable("hosts.count", "3");
        installData.setVariable("host.1", "node1");
        installData.setVariable("host.2", "node2");
        installData.setVariable("host.3", "node3");

        assignHostsAction.executeAction(installData, null);

        assertThat(installData.getVariable("hdfs.nn.host"))
                .isEqualTo("node1");
    }

    @Test
    public void executeAction_WithStandalone_SetsCurrentHostname() throws Exception {
        installData.setVariable("hosts", "node1,node2,node3");
        installData.setVariable("distributed", "false");
        assignHostsAction.executeAction(installData, null);

        assertThat(installData.getVariable("hdfs.nn.host"))
                .isEqualTo(InetAddress.getLocalHost().getHostName());
    }
}
