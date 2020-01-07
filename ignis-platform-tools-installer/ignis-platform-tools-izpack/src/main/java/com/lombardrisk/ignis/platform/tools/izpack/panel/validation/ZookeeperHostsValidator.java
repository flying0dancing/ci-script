package com.lombardrisk.ignis.platform.tools.izpack.panel.validation;

import com.izforge.izpack.api.data.InstallData;
import com.lombardrisk.ignis.izpack.panel.validation.SimpleDataValidator;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.lombardrisk.ignis.config.VariableConstants.DISTRIBUTED;
import static com.lombardrisk.ignis.config.VariableConstants.ZOOKEEPER_HOSTS;
import static com.lombardrisk.ignis.platform.tools.izpack.core.InstallerHelper.COMMA;
import static java.lang.Boolean.parseBoolean;

public class ZookeeperHostsValidator extends SimpleDataValidator {

    private static final Predicate<Long> ZOOKEEPER_HOSTS_IS_EVEN =
            zookeeperHostsNumber -> zookeeperHostsNumber % 2 == 0;

    @Override
    public Stream<Optional<String>> validate(final InstallData installData) {
        if (!parseBoolean(installData.getVariable(DISTRIBUTED))) {
            return Stream.empty();
        }
        String[] hosts = installData.getVariable(ZOOKEEPER_HOSTS).split(COMMA);
        long hostCount = hosts.length;

        if (ZOOKEEPER_HOSTS_IS_EVEN.test(hostCount)) {
            return Stream.of(
                    Optional.of("[" + ZOOKEEPER_HOSTS + "] An odd number of ZooKeeper hosts must be provided"
                            + ". "
                            + "Found " + hostCount));
        }
        return Stream.empty();
    }
}
