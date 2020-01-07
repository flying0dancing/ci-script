package com.lombardrisk.ignis.platform.tools.izpack.panel.processor;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.panels.userinput.processor.Processor;
import com.izforge.izpack.panels.userinput.processorclient.ProcessingClient;
import com.lombardrisk.ignis.platform.tools.izpack.core.InstallerHelper;

import java.util.ArrayList;
import java.util.List;

import static com.lombardrisk.ignis.config.VariableConstants.HOSTS_COUNT;
import static com.lombardrisk.ignis.config.VariableConstants.HOSTS_PREFIX;

public class LoadServerIpProcessor implements Processor {

    private final InstallData installData;

    public LoadServerIpProcessor(final InstallData installData) {
        this.installData = installData;
    }

    public String process(final ProcessingClient client) {
        InstallerHelper installerHelper = new InstallerHelper(installData);

        if (!installerHelper.isDistributed()) {
            return "localhost";
        }
        int hostCount = Integer.parseInt(installData.getVariable(HOSTS_COUNT));
        List<String> hostVars = new ArrayList<>();

        for (int i = 1; i <= hostCount; i++) {
            String host = HOSTS_PREFIX + i;
            if (installData.getVariable(host) != null) {
                hostVars.add(installData.getVariable(host));
            }
        }
        return String.join(":", hostVars);
    }
}
