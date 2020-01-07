package com.lombardrisk.ignis.web.common.config;

import com.lombardrisk.ignis.feature.IgnisFeature;
import org.togglz.core.Feature;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.file.FileBasedStateRepository;
import org.togglz.core.user.UserProvider;

import java.io.File;

public class TogglzConfiguration implements TogglzConfig {

    private final File featurePropertiesFile;
    private final UserProvider userProvider;

    public TogglzConfiguration(final File featurePropertiesFile, final UserProvider userProvider) {
        this.featurePropertiesFile = featurePropertiesFile;
        this.userProvider = userProvider;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
        return IgnisFeature.class;
    }

    @Override
    public StateRepository getStateRepository() {
        return new FileBasedStateRepository(featurePropertiesFile);
    }

    @Override
    public UserProvider getUserProvider() {
        return userProvider;
    }

    public File getFeaturePropertiesFile() {
        return featurePropertiesFile;
    }
}
