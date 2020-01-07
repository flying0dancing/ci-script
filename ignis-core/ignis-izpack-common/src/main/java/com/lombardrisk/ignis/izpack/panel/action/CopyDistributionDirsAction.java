package com.lombardrisk.ignis.izpack.panel.action;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.data.PanelAction;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.lombardrisk.ignis.izpack.core.InstallerProps.DIST_PROP_PREFIX;
import static com.lombardrisk.ignis.izpack.core.InstallerProps.INSTALLER_ROOT_PATH_PROP;
import static org.apache.commons.io.FileUtils.copyDirectory;

@Slf4j
public class CopyDistributionDirsAction implements PanelAction {

    @Override
    public void initialize(final PanelActionConfiguration configuration) {
    }

    @Override
    public void executeAction(final InstallData installData, final AbstractUIHandler handler) {
        installData.getVariables().getProperties().stringPropertyNames()
                .stream().parallel()
                .filter(installProperty -> installProperty.startsWith(DIST_PROP_PREFIX))
                .forEach(distDirProperty -> copyDistDir(installData, distDirProperty));
    }

    private static void copyDistDir(final InstallData installData, final String distDirProperty) {
        String distDir = installData.getVariable(distDirProperty);

        Path sourceDir = Paths.get(installData.getVariable(INSTALLER_ROOT_PATH_PROP), distDir);
        Path targetDir = Paths.get(installData.getInstallPath(), distDir);

        logCopy(distDirProperty, sourceDir, targetDir);
        try {
            copyDirectory(sourceDir.toFile(), targetDir.toFile());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new InstallerException(e);
        }
    }

    private static void logCopy(final String distDirProperty, final Path sourceDir, final Path targetDir) {
        log.debug("Copy distribution [{}] from [{}]", distDirProperty, sourceDir);

        log.info("Copy [{}]\n  to [{}]", sourceDir, targetDir);
    }
}
