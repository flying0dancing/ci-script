package com.lombardrisk.ignis.platform.tools.izpack.panel.validation;

import com.izforge.izpack.api.data.InstallData;
import com.lombardrisk.ignis.izpack.panel.validation.SimpleDataValidator;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.lombardrisk.ignis.config.VariableConstants.HOSTS;
import static com.lombardrisk.ignis.platform.tools.izpack.core.InstallerHelper.COMMA;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
public class LocalHostAddressValidator extends SimpleDataValidator {

    @Override
    public Stream<Optional<String>> validate(final InstallData installData) {
        Set<String> hostProps = installData.getVariables().getProperties().stringPropertyNames()
                .stream()
                .filter(this::isHostProperty)
                .filter(prop -> isNotBlank(installData.getVariable(prop)))
                .collect(toSet());

        return hostProps.stream()
                .map(hostProp -> validateHost(hostProp, installData));
    }

    private boolean isHostProperty(final String prop) {
        return prop.equals(HOSTS)
                || prop.endsWith(".host")
                || prop.endsWith(".hosts");
    }

    private static Optional<String> validateHost(final String hostProp, final InstallData installData) {
        String[] hosts = installData.getVariable(hostProp).split(COMMA);

        for (String host : hosts) {
            if (isLocalHost(host)) {
                return Optional.of("[" + hostProp + "] All hosts must be different than '127.0.0.1' or 'localhost'");
            }
            Optional<String> optionalInvalidAddress = validateIpAddress(hostProp, host);
            if (optionalInvalidAddress.isPresent()) {
                return optionalInvalidAddress;
            }
        }
        return Optional.empty();
    }

    private static boolean isLocalHost(final String host) {
        return "127.0.0.1".equalsIgnoreCase(host)
                || "localhost".equalsIgnoreCase(host);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static Optional<String> validateIpAddress(final String hostProp, final String host) {
        try {
            InetAddress.getByName(host);
            return Optional.empty();
        } catch (@SuppressWarnings("squid:S1166") UnknownHostException e) {
            return Optional.of("[" + hostProp + "] '" + host + "' must be a host that the installer can connect to");
        }
    }
}
