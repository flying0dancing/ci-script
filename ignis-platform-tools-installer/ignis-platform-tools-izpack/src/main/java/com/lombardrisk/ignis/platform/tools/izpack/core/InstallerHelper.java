package com.lombardrisk.ignis.platform.tools.izpack.core;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.installer.bootstrap.Installer;

import static com.lombardrisk.ignis.config.VariableConstants.DISTRIBUTED;
import static java.lang.Boolean.parseBoolean;

public class InstallerHelper {

    private final InstallData installData;

    public static final String COMMA = ",";

    public InstallerHelper(final InstallData installData) {
        this.installData = installData;
    }

    public static boolean isAutoInstall() {
        return Installer.getInstallerMode() == Installer.INSTALLER_AUTO;
    }

    public boolean isDistributed() {
        return parseBoolean(installData.getVariable(DISTRIBUTED));
    }
}
