package com.lombardrisk.ignis.izpack.panel.validation;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator.Status;
import com.lombardrisk.ignis.izpack.InstallDataFixtures;
import org.junit.Test;

import static com.lombardrisk.ignis.izpack.core.InstallerProps.DATASET_SOURCE_LOCAL_PATH_PROP;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;

public class LocalDatasourceValidatorTest {

    private InstallData installData = InstallDataFixtures.installData();
    private LocalDatasourceValidator localDatasourceValidator = new LocalDatasourceValidator();

    @Test
    public void validateData_ReturnsOK() {
        installData.setVariable(DATASET_SOURCE_LOCAL_PATH_PROP, "target/datasets");

        assertThat(localDatasourceValidator.validateData(installData))
                .isEqualTo(Status.OK);
    }

    @Test
    public void validateData_BlankLocalPath_ReturnsError() {
        installData.setVariable(DATASET_SOURCE_LOCAL_PATH_PROP, null);

        assertThat(localDatasourceValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        installData.setVariable(DATASET_SOURCE_LOCAL_PATH_PROP, EMPTY);

        assertThat(localDatasourceValidator.validateData(installData))
                .isEqualTo(Status.ERROR);
        assertThat(localDatasourceValidator.getErrorMessageId())
                .contains(DATASET_SOURCE_LOCAL_PATH_PROP)
                .contains("must not be blank");
    }
}