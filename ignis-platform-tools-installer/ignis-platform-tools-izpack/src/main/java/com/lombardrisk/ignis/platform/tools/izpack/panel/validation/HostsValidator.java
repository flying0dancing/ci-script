package com.lombardrisk.ignis.platform.tools.izpack.panel.validation;

import com.izforge.izpack.api.data.InstallData;
import com.lombardrisk.ignis.izpack.panel.validation.SimpleDataValidator;

import java.util.Optional;
import java.util.stream.Stream;

import static com.lombardrisk.ignis.config.VariableConstants.DISTRIBUTED;
import static com.lombardrisk.ignis.config.VariableConstants.HOSTS;
import static com.lombardrisk.ignis.config.VariableConstants.HOSTS_COUNT;
import static java.lang.Boolean.parseBoolean;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class HostsValidator extends SimpleDataValidator {

    private static final int MIN_HOST_COUNT = 3;

    @Override
    public Stream<Optional<String>> validate(final InstallData installData) {
        boolean distributedMode = parseBoolean(installData.getVariable(DISTRIBUTED));
        if (!distributedMode) {
            return Stream.empty();
        }
        return Stream.of(
                validateHosts(installData),
                validateHostCount(installData));
    }

    private static Optional<String> validateHosts(final InstallData installData) {
        if (isBlank(installData.getVariable(HOSTS))) {
            return Optional.of("[" + HOSTS + "] Hosts must not be blank");
        }
        return Optional.empty();
    }

    private static Optional<String> validateHostCount(final InstallData installData) {
        String hostCountValue = installData.getVariable(HOSTS_COUNT);

        if (isNotBlank(hostCountValue)
                && Integer.valueOf(hostCountValue) < MIN_HOST_COUNT) {
            return Optional.of("[" + HOSTS_COUNT + "] At least 3 unique hosts must be provided "
                    + "when distributed mode is selected. Found " + hostCountValue);
        }
        return Optional.empty();
    }
}
