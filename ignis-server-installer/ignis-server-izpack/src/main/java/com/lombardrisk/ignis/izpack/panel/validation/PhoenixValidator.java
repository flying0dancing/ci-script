package com.lombardrisk.ignis.izpack.panel.validation;

import com.izforge.izpack.api.data.InstallData;

import java.util.Optional;
import java.util.stream.Stream;

import static com.lombardrisk.ignis.izpack.core.InstallerProps.PHOENIX_QUERY_HOST_PROP;
import static com.lombardrisk.ignis.izpack.core.InstallerProps.PHOENIX_QUERY_PORT_PROP;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class PhoenixValidator extends SimpleDataValidator {

    @Override
    public Stream<Optional<String>> validate(final InstallData installData) {
        return Stream.of(
                validateHost(installData),
                validatePort(installData));
    }

    private static Optional<String> validateHost(final InstallData installData) {
        if (isBlank(installData.getVariable(PHOENIX_QUERY_HOST_PROP))) {
            return Optional.of("[" + PHOENIX_QUERY_HOST_PROP + "] Phoenix query server host must not be blank");
        }
        return Optional.empty();
    }

    private static Optional<String> validatePort(final InstallData installData) {
        String portValue = installData.getVariable(PHOENIX_QUERY_PORT_PROP);

        if (isBlank(portValue)) {
            return Optional.of("[" + PHOENIX_QUERY_PORT_PROP + "] Phoenix query server port must not be blank");
        }
        if (isInvalidPort(portValue)) {
            return Optional.of("[" + PHOENIX_QUERY_PORT_PROP + "] "
                    + "Phoenix query server port must be a number between 0 and 65535");
        }
        return Optional.empty();
    }
}
