package com.lombardrisk.ignis.platform.tools.izpack.panel.validation;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator.Status;
import com.lombardrisk.ignis.platform.tools.izpack.InstallDataFixtures;
import org.junit.Test;

import static com.lombardrisk.ignis.config.VariableConstants.HBASE_MASTER_HOST;
import static com.lombardrisk.ignis.config.VariableConstants.HBASE_REGIONSERVER_HOSTS;
import static com.lombardrisk.ignis.config.VariableConstants.HDFS_NN_HOST;
import static com.lombardrisk.ignis.config.VariableConstants.HISTORY_SERVER_HOST;
import static com.lombardrisk.ignis.config.VariableConstants.HOSTS;
import static com.lombardrisk.ignis.config.VariableConstants.PHOENIX_QS_HOSTS;
import static com.lombardrisk.ignis.config.VariableConstants.SPARK_HISTORY_SERVER_HOST;
import static com.lombardrisk.ignis.config.VariableConstants.YARN_RM_HOST;
import static org.assertj.core.api.Assertions.assertThat;

public class LocalHostAddressValidatorTest {

    private InstallData installData = InstallDataFixtures.installData();
    private LocalHostAddressValidator validator = new LocalHostAddressValidator();

    @Test
    public void validateData_ReturnsOK() {
        installData.setVariable(SPARK_HISTORY_SERVER_HOST, "10.0.0.1");
        installData.setVariable(HISTORY_SERVER_HOST, "20.0.0.1");

        assertThat(validator.validateData(installData))
                .isEqualTo(Status.OK);
    }

    @Test
    public void validateData_LoopbackIp_ReturnsError() {
        installData.setVariable(HBASE_MASTER_HOST, "10.0.0.1");
        installData.setVariable(HOSTS, "12.20.20.1,127.0.0.1");

        assertThat(validator.validateData(installData))
                .isEqualTo(Status.ERROR);
    }

    @Test
    public void validateData_Localhost_ReturnsError() {
        installData.setVariable(PHOENIX_QS_HOSTS, "10.0.0.1");
        installData.setVariable(YARN_RM_HOST, "localhost");

        assertThat(validator.validateData(installData))
                .isEqualTo(Status.ERROR);
    }

    @Test
    public void validateData_InvalidIp_ReturnsError() {
        installData.setVariable(HDFS_NN_HOST, "910.0.0.1");
        installData.setVariable(HBASE_REGIONSERVER_HOSTS, "20.0.0.1");

        assertThat(validator.validateData(installData))
                .isEqualTo(Status.ERROR);
    }
}