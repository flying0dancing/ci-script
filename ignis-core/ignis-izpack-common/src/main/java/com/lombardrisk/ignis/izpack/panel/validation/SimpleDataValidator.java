package com.lombardrisk.ignis.izpack.panel.validation;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.math.NumberUtils.isDigits;

@Slf4j
public abstract class SimpleDataValidator implements DataValidator {

    private static final int MIN_PORT = 0;
    private static final int MAX_PORT = 65535;

    private List<String> errors;

    public abstract Stream<Optional<String>> validate(final InstallData installData);

    @Override
    public Status validateData(final InstallData installData) {
        errors = validate(installData)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());

        if (errors.isEmpty()) {
            return Status.OK;
        } else {
            log.debug(getErrorMessageId());
            return Status.ERROR;
        }
    }

    @Override
    public String getErrorMessageId() {
        return String.join(";\n ", this.errors);
    }

    @Override
    public String getWarningMessageId() {
        return null;
    }

    @Override
    public boolean getDefaultAnswer() {
        return false;
    }

    static boolean isInvalidPort(final String value) {
        if (!isDigits(value)) {
            return true;
        }
        int port = Integer.parseInt(value);
        return port < MIN_PORT || port > MAX_PORT;
    }
}
