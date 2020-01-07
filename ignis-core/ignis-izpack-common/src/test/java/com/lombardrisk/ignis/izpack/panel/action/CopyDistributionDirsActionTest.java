package com.lombardrisk.ignis.izpack.panel.action;

import com.izforge.izpack.api.data.InstallData;
import com.lombardrisk.ignis.izpack.InstallDataFixtures;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static com.lombardrisk.ignis.izpack.core.InstallerProps.INSTALLER_ROOT_PATH_PROP;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static org.assertj.core.api.Assertions.assertThat;

public class CopyDistributionDirsActionTest {

    private InstallData installData = InstallDataFixtures.installData();
    private CopyDistributionDirsAction copyDistributionDirsAction = new CopyDistributionDirsAction();

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void executeAction() throws IOException {
        Path installerDir = temp.newFolder("installer").toPath();

        createDirectories(installerDir.resolve("server/lib"));
        createFile(installerDir.resolve("server/lib/spark.app.jar"));

        createDirectories(installerDir.resolve("migrations/drivers"));
        createFile(installerDir.resolve("migrations/drivers/h2.jar"));

        installData.setVariable(INSTALLER_ROOT_PATH_PROP, installerDir.toAbsolutePath().toString());

        File installPathDir = temp.newFolder("fcr-engine");
        installData.setInstallPath(installPathDir.getAbsolutePath());

        installData.setVariable("izpack.dist.first", "server");
        installData.setVariable("izpack.dist.second", "migrations");

        copyDistributionDirsAction.executeAction(installData, null);

        assertThat(installPathDir.toPath().resolve("server/lib/spark.app.jar"))
                .exists();
        assertThat(installPathDir.toPath().resolve("migrations/drivers/h2.jar"))
                .exists();
    }
}