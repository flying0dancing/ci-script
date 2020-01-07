package com.lombardrisk.ignis.platform.tools.izpack.panel.validation;

import com.izforge.izpack.api.data.InstallData;
import com.lombardrisk.ignis.izpack.panel.validation.SimpleDataValidator;

import java.util.Optional;
import java.util.stream.Stream;

import static com.lombardrisk.ignis.config.VariableConstants.DISTRIBUTED;
import static com.lombardrisk.ignis.config.VariableConstants.HBASE_BACKUP_MASTER_HOSTS;
import static com.lombardrisk.ignis.config.VariableConstants.HBASE_MASTER_HOST;
import static com.lombardrisk.ignis.platform.tools.izpack.core.InstallerHelper.COMMA;
import static com.lombardrisk.ignis.platform.tools.izpack.core.IpAddress.isSameIpAddress;
import static java.lang.Boolean.parseBoolean;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class HBaseHostValidator extends SimpleDataValidator {

    @Override
    public Stream<Optional<String>> validate(final InstallData installData) {
        String backupHostsProp = installData.getVariable(HBASE_BACKUP_MASTER_HOSTS);
        if (!parseBoolean(installData.getVariable(DISTRIBUTED))
                || isBlank(backupHostsProp)) {
            return Stream.empty();
        }
        String hbaseMasterHost = installData.getVariable(HBASE_MASTER_HOST);

        String[] backupHosts = backupHostsProp.split(COMMA);

        for (String backupHost : backupHosts) {
            if (backupHost.equals(hbaseMasterHost)
                    || isSameIpAddress(hbaseMasterHost, backupHost)) {
                return Stream.of(
                        Optional.of("[" + HBASE_BACKUP_MASTER_HOSTS + "] "
                                + "The HBase backup host/s must be different to the HBase master host/s"));
            }
        }
        return Stream.empty();
    }
}
