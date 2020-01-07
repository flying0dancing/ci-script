package com.lombardrisk.ignis.izpack.panel.validation;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator.Status;
import com.lombardrisk.ignis.izpack.InstallDataFixtures;
import org.junit.Before;
import org.junit.Test;

import static com.lombardrisk.ignis.izpack.core.InstallerProps.HADOOP_FS_PROP;
import static com.lombardrisk.ignis.izpack.core.InstallerProps.HADOOP_USER;
import static com.lombardrisk.ignis.izpack.core.InstallerProps.YARN_RESOURCE_MANAGER_HOST_PROP;
import static com.lombardrisk.ignis.izpack.core.InstallerProps.ZOOKEEPER_CLIENT_PORT_PROP;
import static com.lombardrisk.ignis.izpack.core.InstallerProps.ZOOKEEPER_HOSTS_PROP;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;

public class HadoopValidatorTest {

    private HadoopValidator hadoopValidator = new HadoopValidator();
    private InstallData installData = InstallDataFixtures.installData();

    @Before
    public void setup() {
        installData.setVariable(HADOOP_USER, "hadoop");
        installData.setVariable(HADOOP_FS_PROP, "hdfs://a.b:33");
        installData.setVariable(YARN_RESOURCE_MANAGER_HOST_PROP, "a.b");
        installData.setVariable(ZOOKEEPER_HOSTS_PROP, "a,b");
        installData.setVariable(ZOOKEEPER_CLIENT_PORT_PROP, "7654");
    }

    @Test
    public void validateData_ReturnsOK() {
        installData.setVariable(HADOOP_USER, "linux");
        installData.setVariable(HADOOP_FS_PROP, "hdfs://a.b:33");
        installData.setVariable(YARN_RESOURCE_MANAGER_HOST_PROP, "a.com");
        installData.setVariable(ZOOKEEPER_HOSTS_PROP, "c");
        installData.setVariable(ZOOKEEPER_CLIENT_PORT_PROP, "8765");

        assertThat(hadoopValidator.validateData(installData))
                .isEqualTo(Status.OK);
    }

    @Test
    public void validateData_BlankHadoopFilesystemProp_ReturnsError() {
        installData.setVariable(HADOOP_FS_PROP, EMPTY);
        assertThat(hadoopValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        installData.setVariable(HADOOP_FS_PROP, null);

        assertThat(hadoopValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        assertThat(hadoopValidator.getErrorMessageId())
                .contains(HADOOP_FS_PROP)
                .contains("must not be blank");
    }

    @Test
    public void validateData_InvalidHadoopFilesystemProp_ReturnsError() {
        installData.setVariable(HADOOP_FS_PROP, "fs://a.b:33");

        assertThat(hadoopValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        assertThat(hadoopValidator.getErrorMessageId())
                .contains(HADOOP_FS_PROP)
                .contains("must start with hdfs://");
    }

    @Test
    public void validateData_BlankYarnResourceManagerProp_ReturnsError() {
        installData.setVariable(YARN_RESOURCE_MANAGER_HOST_PROP, EMPTY);
        assertThat(hadoopValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        installData.setVariable(YARN_RESOURCE_MANAGER_HOST_PROP, null);

        assertThat(hadoopValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        assertThat(hadoopValidator.getErrorMessageId())
                .contains(YARN_RESOURCE_MANAGER_HOST_PROP)
                .contains("must not be blank");
    }

    @Test
    public void validateData_BlankHadoopUser_ReturnsError() {
        installData.setVariable(HADOOP_USER, EMPTY);
        assertThat(hadoopValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        installData.setVariable(HADOOP_USER, null);

        assertThat(hadoopValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        assertThat(hadoopValidator.getErrorMessageId())
                .contains(HADOOP_USER)
                .contains("must not be blank");
    }

    @Test
    public void validateData_BlankZooKeeperHosts_ReturnsError() {
        installData.setVariable(ZOOKEEPER_HOSTS_PROP, EMPTY);
        assertThat(hadoopValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        installData.setVariable(ZOOKEEPER_HOSTS_PROP, null);

        assertThat(hadoopValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        installData.setVariable(ZOOKEEPER_HOSTS_PROP, ",");

        assertThat(hadoopValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        assertThat(hadoopValidator.getErrorMessageId())
                .contains(ZOOKEEPER_HOSTS_PROP)
                .contains("must not be blank");
    }

    @Test
    public void validateData_BlankZooKeeperPort_ReturnsError() {
        installData.setVariable(ZOOKEEPER_CLIENT_PORT_PROP, EMPTY);
        assertThat(hadoopValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        installData.setVariable(ZOOKEEPER_CLIENT_PORT_PROP, null);

        assertThat(hadoopValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        assertThat(hadoopValidator.getErrorMessageId())
                .contains(ZOOKEEPER_CLIENT_PORT_PROP)
                .contains("must not be blank");
    }

    @Test
    public void validateData_InvalidHttpsPort_ReturnsError() {
        installData.setVariable(ZOOKEEPER_CLIENT_PORT_PROP, "8o");

        assertThat(hadoopValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        installData.setVariable(ZOOKEEPER_CLIENT_PORT_PROP, "80000");

        assertThat(hadoopValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        installData.setVariable(ZOOKEEPER_CLIENT_PORT_PROP, "-80");

        assertThat(hadoopValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        assertThat(hadoopValidator.getErrorMessageId())
                .contains(ZOOKEEPER_CLIENT_PORT_PROP)
                .contains("must be a number between 0 and 65535");
    }
}