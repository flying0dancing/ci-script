package com.lombardrisk.ignis.platform.tools.izpack.panel.validation;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator.Status;
import com.lombardrisk.ignis.platform.tools.izpack.InstallDataFixtures;
import org.junit.Test;

import static com.lombardrisk.ignis.config.VariableConstants.DISTRIBUTED;
import static com.lombardrisk.ignis.config.VariableConstants.HBASE_BACKUP_MASTER_HOSTS;
import static com.lombardrisk.ignis.config.VariableConstants.HBASE_MASTER_HOST;
import static org.assertj.core.api.Assertions.assertThat;

public class HBaseHostValidatorTest {

    private InstallData installData = InstallDataFixtures.installData();
    private HBaseHostValidator hBaseHostValidator = new HBaseHostValidator();

    @Test
    public void validateData_StandaloneMode_ReturnsOk() {
        installData.setVariable(DISTRIBUTED, "false");
        installData.setVariable(HBASE_MASTER_HOST, "10.0.0.1");
        installData.setVariable(HBASE_BACKUP_MASTER_HOSTS, "10.0.0.1");

        assertThat(hBaseHostValidator.validateData(installData))
                .isEqualTo(Status.OK);
    }

    @Test
    public void validateData_DistributedMode_ReturnsOK() {
        installData.setVariable(DISTRIBUTED, "true");
        installData.setVariable(HBASE_MASTER_HOST, "10.0.0.1");
        installData.setVariable(HBASE_BACKUP_MASTER_HOSTS, "10.0.0.2,10.0.0.3");

        assertThat(hBaseHostValidator.validateData(installData))
                .isEqualTo(Status.OK);
    }

    @Test
    public void validateData_BackupSameAsMasterHost_ReturnsError() {
        installData.setVariable(DISTRIBUTED, "true");
        installData.setVariable(HBASE_MASTER_HOST, "10.0.0.1");
        installData.setVariable(HBASE_BACKUP_MASTER_HOSTS, "10.0.0.2,10.0.0.1");

        assertThat(hBaseHostValidator.validateData(installData))
                .isEqualTo(Status.ERROR);
    }
}