package com.lombardrisk.ignis.izpack.panel.validation;

import com.izforge.izpack.api.data.InstallData;

import java.util.Optional;
import java.util.stream.Stream;

import static com.lombardrisk.ignis.izpack.core.InstallerProps.HADOOP_FS_PROP;
import static com.lombardrisk.ignis.izpack.core.InstallerProps.HADOOP_USER;
import static com.lombardrisk.ignis.izpack.core.InstallerProps.YARN_RESOURCE_MANAGER_HOST_PROP;
import static com.lombardrisk.ignis.izpack.core.InstallerProps.ZOOKEEPER_CLIENT_PORT_PROP;
import static com.lombardrisk.ignis.izpack.core.InstallerProps.ZOOKEEPER_HOSTS_PROP;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.startsWith;

public class HadoopValidator extends SimpleDataValidator {

    @Override
    public Stream<Optional<String>> validate(final InstallData installData) {
        return Stream.of(
                validateZooKeeperHosts(installData),
                validateZooKeeperPort(installData),
                validateHadoopUser(installData),
                validateHadoopFilesystemURL(installData),
                validateYarnHost(installData));
    }

    private static Optional<String> validateZooKeeperHosts(final InstallData installData) {
        String zookeeperHostsValue = installData.getVariable(ZOOKEEPER_HOSTS_PROP);

        if (isBlank(zookeeperHostsValue)) {
            return Optional.of("[" + ZOOKEEPER_HOSTS_PROP + "] ZooKeeper hosts must not be blank");
        }
        String[] zookeeperHosts = zookeeperHostsValue.split(",");

        if (zookeeperHosts.length == 0) {
            return Optional.of("[" + ZOOKEEPER_HOSTS_PROP + "] ZooKeeper hosts must not be blank");
        }
        return Optional.empty();
    }

    private static Optional<String> validateZooKeeperPort(final InstallData installData) {
        String zookeeperPort = installData.getVariable(ZOOKEEPER_CLIENT_PORT_PROP);

        if (isBlank(zookeeperPort)) {
            return Optional.of("[" + ZOOKEEPER_CLIENT_PORT_PROP + "] ZooKeeper client port must not be blank");
        }
        if (isInvalidPort(zookeeperPort)) {
            return Optional.of("[" + ZOOKEEPER_CLIENT_PORT_PROP + "] "
                    + "ZooKeeper client port must be a number between 0 and 65535");
        }
        return Optional.empty();
    }

    private static Optional<String> validateHadoopUser(final InstallData installData) {
        if (isBlank(installData.getVariable(HADOOP_USER))) {
            return Optional.of("[" + HADOOP_USER + "] Hadoop user must not be blank");
        }
        return Optional.empty();
    }

    private static Optional<String> validateHadoopFilesystemURL(final InstallData installData) {
        String hadoopFsUrl = installData.getVariable(HADOOP_FS_PROP);

        if (isBlank(hadoopFsUrl)) {
            return Optional.of("[" + HADOOP_FS_PROP + "] Hadoop filesystem URL must not be blank");
        }
        if (!startsWith(hadoopFsUrl, "hdfs://")) {
            return Optional.of("[" + HADOOP_FS_PROP + "] Hadoop filesystem URL must start with hdfs://");
        }
        return Optional.empty();
    }

    private static Optional<String> validateYarnHost(final InstallData installData) {
        if (isBlank(installData.getVariable(YARN_RESOURCE_MANAGER_HOST_PROP))) {
            return Optional.of("["
                    + YARN_RESOURCE_MANAGER_HOST_PROP
                    + "] Yarn Resource Manager host must not be blank");
        }
        return Optional.empty();
    }
}
