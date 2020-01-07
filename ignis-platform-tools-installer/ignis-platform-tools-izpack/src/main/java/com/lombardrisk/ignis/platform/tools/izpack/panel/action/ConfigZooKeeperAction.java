package com.lombardrisk.ignis.platform.tools.izpack.panel.action;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.data.PanelAction;
import com.lombardrisk.ignis.platform.tools.izpack.core.InstallerHelper;
import com.lombardrisk.ignis.platform.tools.izpack.core.IpAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.lombardrisk.ignis.config.VariableConstants.ZOOKEEPER_HOSTS;
import static com.lombardrisk.ignis.config.VariableConstants.ZOOKEEPER_MYID;
import static com.lombardrisk.ignis.platform.tools.izpack.core.InstallerHelper.COMMA;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class ConfigZooKeeperAction implements PanelAction {

    @Override
    public void executeAction(final InstallData installData, final AbstractUIHandler handler) {
        installData.setVariable(ZOOKEEPER_MYID, EMPTY);
        InstallerHelper installerHelper = new InstallerHelper(installData);

        if (!installerHelper.isDistributed()
                || toZookeeperHosts(installData).size() <= 1) {
            return;
        }

        Optional<Integer> zookeeperId = getZookeeperId(installData);

        if (zookeeperId.isPresent()) {
            Integer myId = zookeeperId.get();
            installData.setVariable(ZOOKEEPER_MYID, String.valueOf(myId));
        }
    }

    private static Optional<Integer> getZookeeperId(final InstallData installData) {
        List<String> zookeeperHosts = toZookeeperHosts(installData);

        Optional<String> myIp = zookeeperHosts.stream()
                .filter(ConfigZooKeeperAction::isCurrentIpAddress)
                .findFirst();

        return myIp.map(s -> zookeeperHosts.indexOf(s) + 1);
    }

    /**
     * The zookeeper hosts must be iterated over in the same order in which they are declared.
     * The script zookeeper-3.4.10\post-install.sh iterates over these hosts in insertion order.
     *
     * This script then places the server1, server2 ... properties in the zoo.cfg which must match
     * the ignis-installer\src\main\izpack\dist\data\zookeeper\myid file that tells zookeeper which server it is
     * (placed in zookeeper's data directory by post-install.sh).
     *
     * i.e.
     * If myId = 1 on host1 and server.1=host2 then zookeeper wont start.
     *
     * For each zookeeper host there must be a myId file whose content number must match that that is assigned in
     * zoo.cfg
     *
     * @param installData The install data taken from ui or properties file
     * @return List of zookeeper hosts
     * @link https://zookeeper.apache.org/doc/r3.3.3/zookeeperAdmin.html
     */
    private static List<String> toZookeeperHosts(final InstallData installData) {
        return Arrays.stream(installData.getVariable(ZOOKEEPER_HOSTS).split(COMMA))
                .collect(Collectors.toList());
    }

    @Override
    public void initialize(final PanelActionConfiguration configuration) {
        // no-op
    }

    private static boolean isCurrentIpAddress(final String host) {
        try {
            return IpAddress.isCurrentIpAddress(InetAddress.getByName(host));
        } catch (final UnknownHostException e) {
            throw new IllegalStateException("Unknown Host in zookeepers hosts: " + host, e);
        }
    }
}
