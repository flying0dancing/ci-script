package com.lombardrisk.ignis.izpack.panel.validation;

import com.izforge.izpack.api.data.InstallData;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.lombardrisk.ignis.izpack.core.InstallerProps.DIST_PROP_PREFIX;
import static com.lombardrisk.ignis.izpack.core.InstallerProps.INSTALLER_ROOT_PATH_PROP;
import static java.nio.file.Files.isDirectory;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
public class InstallerStructureValidator extends SimpleDataValidator {

    @Override
    public Stream<Optional<String>> validate(final InstallData installData) {
        String installerPathProp = installData.getVariable(INSTALLER_ROOT_PATH_PROP);
        if (isBlank(installerPathProp)) {
            return Stream.of(Optional.of("[" + INSTALLER_ROOT_PATH_PROP + "] Installer root path must not be blank"));
        }
        Path installerRootPath = Paths.get(installerPathProp);
        if (!installerRootPath.toFile().exists()) {
            return Stream.of(Optional.of("[" + INSTALLER_ROOT_PATH_PROP + "] "
                    + "Installer root path '" + installerRootPath + "' must exist"));
        }
        Set<String> distDirProps = findDistributionDirProps(installData);
        if (distDirProps.isEmpty()) {
            return Stream.of(Optional.of("[" + DIST_PROP_PREFIX + "*] at least 1 distribution dir must be provided"));
        }
        return distDirProps.stream()
                .map(distDirProperty -> validateDistributionDir(installerRootPath, installData, distDirProperty));
    }

    private static Set<String> findDistributionDirProps(final InstallData installData) {
        return installData.getVariables().getProperties().stringPropertyNames()
                .stream()
                .filter(installProperty -> installProperty.startsWith(DIST_PROP_PREFIX))
                .collect(toSet());
    }

    private static Optional<String> validateDistributionDir(
            final Path installerRootPath,
            final InstallData installData,
            final String distDirProperty) {
        String distDir = installData.getVariable(distDirProperty);

        Path installerRelativeDir = installerRootPath.resolve(distDir);

        if (!isDirectory(installerRelativeDir)) {
            return Optional.of("[" + distDirProperty + "] cannot be found at " + installerRelativeDir);
        }
        log.debug("Check distribution dir exists [{}]", installerRelativeDir);
        return Optional.empty();
    }
}
