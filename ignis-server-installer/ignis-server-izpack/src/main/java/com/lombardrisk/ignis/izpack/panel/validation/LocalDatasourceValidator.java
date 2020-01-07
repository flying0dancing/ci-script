package com.lombardrisk.ignis.izpack.panel.validation;

import com.izforge.izpack.api.data.InstallData;

import java.util.Optional;
import java.util.stream.Stream;

import static com.lombardrisk.ignis.izpack.core.InstallerProps.DATASET_SOURCE_LOCAL_PATH_PROP;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class LocalDatasourceValidator extends SimpleDataValidator {

    @Override
    public Stream<Optional<String>> validate(final InstallData installData) {
        return Stream.of(
                validateDatasetLocationPath(installData));
    }

    private Optional<String> validateDatasetLocationPath(final InstallData installData) {
        if (isBlank(installData.getVariable(DATASET_SOURCE_LOCAL_PATH_PROP))) {
            return Optional.of("[" + DATASET_SOURCE_LOCAL_PATH_PROP + "] Dataset source path must not be blank");
        }
        return Optional.empty();
    }
}
