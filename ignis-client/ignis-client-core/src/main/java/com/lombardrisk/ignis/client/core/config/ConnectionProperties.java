package com.lombardrisk.ignis.client.core.config;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.net.URL;

@Builder
@Data
public class ConnectionProperties {

    @NonNull
    private final URL url;
    private final String basicAuthUsername;
    private final String basicAuthPassword;
}
