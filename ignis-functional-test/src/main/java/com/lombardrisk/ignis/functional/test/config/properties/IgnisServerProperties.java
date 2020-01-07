package com.lombardrisk.ignis.functional.test.config.properties;

import com.lombardrisk.ignis.client.core.config.ConnectionProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URL;

import static com.google.common.base.MoreObjects.toStringHelper;

@ConfigurationProperties(prefix = "ignis-server")
public class IgnisServerProperties {

    private URL url;
    private String basicAuthUsername;
    private String basicAuthPassword;

    public URL getUrl() {
        return url;
    }

    public void setUrl(final URL url) {
        this.url = url;
    }

    public String getBasicAuthUsername() {
        return basicAuthUsername;
    }

    public void setBasicAuthUsername(final String basicAuthUsername) {
        this.basicAuthUsername = basicAuthUsername;
    }

    public String getBasicAuthPassword() {
        return basicAuthPassword;
    }

    public void setBasicAuthPassword(final String basicAuthPassword) {
        this.basicAuthPassword = basicAuthPassword;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("url", url)
                .add("basicAuthUsername", basicAuthUsername)
                .add("basicAuthPassword", basicAuthPassword)
                .toString();
    }

    public ConnectionProperties toConnectionProperties() {
        return ConnectionProperties.builder()
                .url(getUrl())
                .basicAuthUsername(getBasicAuthUsername())
                .basicAuthPassword(getBasicAuthPassword())
                .build();
    }
}
