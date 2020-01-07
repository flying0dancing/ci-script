package com.lombardrisk.ignis.izpack.panel.validation;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator.Status;
import com.lombardrisk.ignis.izpack.InstallDataFixtures;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static com.lombardrisk.ignis.izpack.core.InstallerProps.INSTALLER_ROOT_PATH_PROP;
import static java.nio.file.Files.createDirectories;
import static org.assertj.core.api.Assertions.assertThat;

public class InstallerStructureValidatorTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private InstallerStructureValidator installerStructureValidator = new InstallerStructureValidator();
    private InstallData installData = InstallDataFixtures.installData();

    @Test
    public void validateData_MissingInstallerVariable_ReturnsError() throws IOException {
        installData.setVariable("izpack.dist.1", temp.newFolder().getAbsolutePath());
        installData.setVariable("izpack.dist.2", temp.newFolder().getAbsolutePath());

        Status validatedStatus = installerStructureValidator.validateData(installData);

        assertThat(validatedStatus)
                .isEqualTo(Status.ERROR);
        assertThat(installerStructureValidator.getErrorMessageId())
                .contains(INSTALLER_ROOT_PATH_PROP)
                .contains("must not be blank");
    }

    @Test
    public void validateData_NonExistentInstallerPath_ReturnsError() throws IOException {
        installData.setVariable(INSTALLER_ROOT_PATH_PROP, "install12345/dir332");

        installData.setVariable("izpack.dist.1", temp.newFolder().getAbsolutePath());
        installData.setVariable("izpack.dist.2", temp.newFolder().getAbsolutePath());

        Status validatedStatus = installerStructureValidator.validateData(installData);

        assertThat(validatedStatus)
                .isEqualTo(Status.ERROR);
        assertThat(installerStructureValidator.getErrorMessageId())
                .contains(INSTALLER_ROOT_PATH_PROP)
                .contains("install12345")
                .contains("dir332")
                .contains("must exist");
    }

    @Test
    public void validateData_NonExistentDistributionDir_ReturnsError() throws IOException {
        File installerDir = temp.newFolder();
        installData.setVariable(INSTALLER_ROOT_PATH_PROP, installerDir.getAbsolutePath());

        installData.setVariable("izpack.dist.1", "flyway-dir");
        createDirectories(installerDir.toPath().resolve("ignis"));
        installData.setVariable("izpack.dist.2", "ignis");

        Status validatedStatus = installerStructureValidator.validateData(installData);

        assertThat(validatedStatus)
                .isEqualTo(Status.ERROR);
        assertThat(installerStructureValidator.getErrorMessageId())
                .contains("izpack.dist.1")
                .contains("flyway-dir");
    }

    @Test
    public void validateData_NoDistributionDirs_ReturnsError() throws IOException {
        installData.setVariable(INSTALLER_ROOT_PATH_PROP, temp.newFolder().getAbsolutePath());

        Status validatedStatus = installerStructureValidator.validateData(installData);

        assertThat(validatedStatus)
                .isEqualTo(Status.ERROR);
        assertThat(installerStructureValidator.getErrorMessageId())
                .contains("izpack.dist.*")
                .contains("at least 1")
                .contains("must be provided");
    }

    @Test
    public void validateData_ExistingDirs_ReturnsOK() throws IOException {
        File installerDir = temp.newFolder();
        createDirectories(installerDir.toPath().resolve("ignis"));
        createDirectories(installerDir.toPath().resolve("flyway"));

        installData.setVariable(INSTALLER_ROOT_PATH_PROP, installerDir.getAbsolutePath());
        installData.setVariable("izpack.dist.flyway.dir", "flyway");
        installData.setVariable("izpack.dist.ignis.dir", "ignis");

        Status validatedStatus = installerStructureValidator.validateData(installData);

        assertThat(validatedStatus).isEqualTo(Status.OK);
    }
}