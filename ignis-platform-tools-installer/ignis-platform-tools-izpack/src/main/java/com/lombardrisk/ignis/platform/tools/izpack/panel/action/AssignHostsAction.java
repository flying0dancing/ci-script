package com.lombardrisk.ignis.platform.tools.izpack.panel.action;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.data.PanelAction;
import com.lombardrisk.ignis.platform.tools.izpack.core.Cluster;
import com.lombardrisk.ignis.platform.tools.izpack.core.InstallerHelper;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.StringJoiner;

import static com.lombardrisk.ignis.config.VariableConstants.HBASE_MASTER_HOST;
import static com.lombardrisk.ignis.config.VariableConstants.HBASE_REGIONSERVER_HOSTS;
import static com.lombardrisk.ignis.config.VariableConstants.HDFS_DN_HOSTS;
import static com.lombardrisk.ignis.config.VariableConstants.HDFS_NN_HOST;
import static com.lombardrisk.ignis.config.VariableConstants.HDFS_SN_HOST;
import static com.lombardrisk.ignis.config.VariableConstants.HISTORY_SERVER_HOST;
import static com.lombardrisk.ignis.config.VariableConstants.HOSTS;
import static com.lombardrisk.ignis.config.VariableConstants.HOSTS_COUNT;
import static com.lombardrisk.ignis.config.VariableConstants.HOSTS_PREFIX;
import static com.lombardrisk.ignis.config.VariableConstants.PHOENIX_QS_HOSTS;
import static com.lombardrisk.ignis.config.VariableConstants.PHOENIX_QS_LOAD_BALANCER;
import static com.lombardrisk.ignis.config.VariableConstants.PHOENIX_SALT_BUCKETS;
import static com.lombardrisk.ignis.config.VariableConstants.SPARK_HISTORY_SERVER_HOST;
import static com.lombardrisk.ignis.config.VariableConstants.YARN_NM_HOSTS;
import static com.lombardrisk.ignis.config.VariableConstants.YARN_RM_HOST;
import static com.lombardrisk.ignis.config.VariableConstants.ZOOKEEPER_HOSTS;
import static com.lombardrisk.ignis.platform.tools.izpack.core.Component.DATANODE;
import static com.lombardrisk.ignis.platform.tools.izpack.core.Component.HISTORY_SERVER;
import static com.lombardrisk.ignis.platform.tools.izpack.core.Component.HMASTER;
import static com.lombardrisk.ignis.platform.tools.izpack.core.Component.NAMENODE;
import static com.lombardrisk.ignis.platform.tools.izpack.core.Component.NODE_MANAGER;
import static com.lombardrisk.ignis.platform.tools.izpack.core.Component.QUERY_SERVER;
import static com.lombardrisk.ignis.platform.tools.izpack.core.Component.REGION_SERVER;
import static com.lombardrisk.ignis.platform.tools.izpack.core.Component.RESOURCE_MANAGER;
import static com.lombardrisk.ignis.platform.tools.izpack.core.Component.SECONDARY_NAMENODE;
import static com.lombardrisk.ignis.platform.tools.izpack.core.Component.SPARK_HISTORY_SERVER;
import static com.lombardrisk.ignis.platform.tools.izpack.core.Component.ZOOKEEPER;
import static com.lombardrisk.ignis.platform.tools.izpack.core.InstallerHelper.COMMA;
import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.joining;

@Slf4j
public class AssignHostsAction implements PanelAction {

    @Override
    public void executeAction(final InstallData installData, final AbstractUIHandler handler) {
        InstallerHelper installerHelper = new InstallerHelper(installData);

        if (installerHelper.isDistributed()) {
            String[] hosts = installData.getVariable(HOSTS).split(COMMA);
            logHosts(hosts);

            setHostsVariable(installData);

            if (InstallerHelper.isAutoInstall()) {
                return;
            }
            setDistributedHostVariables(installData, hosts);
        } else {
            setStandaloneHostVariables(installData);
        }
    }

    private static void setDistributedHostVariables(final InstallData installData, final String[] hosts) {
        Cluster cluster = new Cluster(hosts);

        installData.setVariable(HDFS_NN_HOST, cluster.getHost(NAMENODE));
        installData.setVariable(HDFS_SN_HOST, cluster.getHost(SECONDARY_NAMENODE));
        installData.setVariable(YARN_RM_HOST, cluster.getHost(RESOURCE_MANAGER));
        installData.setVariable(HISTORY_SERVER_HOST, cluster.getHost(HISTORY_SERVER));
        installData.setVariable(HBASE_MASTER_HOST, cluster.getHost(HMASTER));
        installData.setVariable(ZOOKEEPER_HOSTS, cluster.getHost(ZOOKEEPER));

        installData.setVariable(HDFS_DN_HOSTS, String.join(COMMA, cluster.getHosts(DATANODE)));
        installData.setVariable(YARN_NM_HOSTS, String.join(COMMA, cluster.getHosts(NODE_MANAGER)));
        installData.setVariable(HBASE_REGIONSERVER_HOSTS, String.join(COMMA, cluster.getHosts(REGION_SERVER)));
        installData.setVariable(PHOENIX_SALT_BUCKETS, calcPhoenixSaltBuckets(installData));
        installData.setVariable(PHOENIX_QS_HOSTS, String.join(COMMA, cluster.getHosts(QUERY_SERVER)));
        installData.setVariable(PHOENIX_QS_LOAD_BALANCER, cluster.getHosts(QUERY_SERVER).get(0));
        installData.setVariable(SPARK_HISTORY_SERVER_HOST, cluster.getHost(SPARK_HISTORY_SERVER));
    }

    private static void setStandaloneHostVariables(final InstallData installData) {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();

            installData.setVariable(HDFS_NN_HOST, hostName);
            installData.setVariable(HDFS_SN_HOST, hostName);
            installData.setVariable(YARN_RM_HOST, hostName);
            installData.setVariable(HISTORY_SERVER_HOST, hostName);
            installData.setVariable(HBASE_MASTER_HOST, hostName);
            installData.setVariable(ZOOKEEPER_HOSTS, hostName);

            installData.setVariable(HDFS_DN_HOSTS, hostName);
            installData.setVariable(YARN_NM_HOSTS, hostName);
            installData.setVariable(HBASE_REGIONSERVER_HOSTS, hostName);
            installData.setVariable(PHOENIX_QS_HOSTS, hostName);
            installData.setVariable(PHOENIX_QS_LOAD_BALANCER, hostName);

            installData.setVariable(SPARK_HISTORY_SERVER_HOST, hostName);
        } catch (UnknownHostException e) {
            throw new InstallerException(e);
        }
    }

    private static void logHosts(final String[] hosts) {
        String hostPerLine =
                Arrays.stream(hosts)
                        .map(host -> "  [" + host + "]")
                        .collect(joining("\n"));
        log.info("Provided hosts: \n{}", hostPerLine);
    }

    private static String calcPhoenixSaltBuckets(final InstallData installData) {
        return Integer.toString(
                installData.getVariable(HBASE_REGIONSERVER_HOSTS).split(COMMA).length);
    }

    private static void setHostsVariable(final InstallData installData) {
        if (!InstallerHelper.isAutoInstall()) {
            int hostCount = parseInt(installData.getVariable(HOSTS_COUNT)) + 1;

            StringJoiner joiner = new StringJoiner(COMMA);
            for (int i = 1; i < hostCount; i++) {
                String variable = installData.getVariable(HOSTS_PREFIX + i);
                joiner.add(variable);
            }
            installData.setVariable(HOSTS, joiner.toString());
        }
    }

    @Override
    public void initialize(final PanelActionConfiguration configuration) {
        // noop
    }
}
