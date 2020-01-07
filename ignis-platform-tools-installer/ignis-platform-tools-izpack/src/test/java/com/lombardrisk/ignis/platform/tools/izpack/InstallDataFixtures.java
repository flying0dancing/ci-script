package com.lombardrisk.ignis.platform.tools.izpack;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.util.Platform;
import lombok.experimental.UtilityClass;

@UtilityClass
public class InstallDataFixtures {

    public static InstallData installData() {
        return new AutomatedInstallData(new DefaultVariables(), new Platform(Platform.Name.UNKNOWN));
    }
}
