package com.lombardrisk.ignis.platform.tools.izpack.panel.action;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.data.PanelAction;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Set;

import static com.lombardrisk.ignis.config.VariableConstants.HOSTS;
import static com.lombardrisk.ignis.config.VariableConstants.HOSTS_COUNT;
import static com.lombardrisk.ignis.config.VariableConstants.HOSTS_PREFIX;
import static com.lombardrisk.ignis.platform.tools.izpack.core.InstallerHelper.COMMA;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
public class LoadHostsAction implements PanelAction {

    @Override
    public void executeAction(final InstallData installData, final AbstractUIHandler abstractUIHandler) {
        String hostsValue = installData.getVariable(HOSTS);
        if (isBlank(hostsValue)) {
            return;
        }
        Set<String> hosts =
                Arrays.stream(hostsValue.split(COMMA))
                        .collect(toSet());

        int hostIndex = 1;
        for (String host : hosts) {
            installData.setVariable(HOSTS_PREFIX + hostIndex, host);
            hostIndex++;
        }
        installData.setVariable(HOSTS_COUNT, String.valueOf(hosts.size()));
    }

    @Override
    public void initialize(final PanelActionConfiguration panelActionConfiguration) {
        // no-op
    }
}
