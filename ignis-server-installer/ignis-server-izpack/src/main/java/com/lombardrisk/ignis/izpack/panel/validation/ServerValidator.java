package com.lombardrisk.ignis.izpack.panel.validation;

import com.izforge.izpack.api.data.InstallData;

import java.util.Optional;
import java.util.stream.Stream;

import static com.lombardrisk.ignis.izpack.core.InstallerProps.SERVER_CONTEXT_PATH_PROP;
import static com.lombardrisk.ignis.izpack.core.InstallerProps.SERVER_HTTPS_PORT_PROP;
import static com.lombardrisk.ignis.izpack.core.InstallerProps.SERVER_HTTP_PORT_PROP;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class ServerValidator extends SimpleDataValidator {

    @Override
    public Stream<Optional<String>> validate(final InstallData installData) {
        return Stream.of(
                validateContextPath(installData),
                validateHttpPort(installData),
                validateHttpsPort(installData));
    }

    private Optional<String> validateContextPath(final InstallData installData) {
        if (isBlank(installData.getVariable(SERVER_CONTEXT_PATH_PROP))) {
            return Optional.of("[" + SERVER_CONTEXT_PATH_PROP + "] Ignis server context path must not be blank");
        }
        return Optional.empty();
    }

    private Optional<String> validateHttpPort(final InstallData installData) {
        String httpPort = installData.getVariable(SERVER_HTTP_PORT_PROP);
        if (isBlank(httpPort)) {
            return Optional.of("[" + SERVER_HTTP_PORT_PROP + "] Ignis server http port must not be blank");
        }

        if (isInvalidPort(httpPort)) {
            return Optional.of("[" + SERVER_HTTP_PORT_PROP + "] "
                    + "Ignis server http port must be a number between 0 and 65535");
        }
        return Optional.empty();
    }

    private Optional<String> validateHttpsPort(final InstallData installData) {
        String httpsPort = installData.getVariable(SERVER_HTTPS_PORT_PROP);
        if (isBlank(httpsPort)) {
            return Optional.of("[" + SERVER_HTTPS_PORT_PROP + "] Ignis server https port must not be blank");
        }
        if (isInvalidPort(httpsPort)) {
            return Optional.of("[" + SERVER_HTTPS_PORT_PROP + "] "
                    + "Ignis server https port must be a number between 0 and 65535");
        }
        return Optional.empty();
    }
}
