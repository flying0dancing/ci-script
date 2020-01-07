package com.lombardrisk.ignis.client.core.config;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ClientContext {

    private final long connectionTimeout;
    private final long requestTimeout;
}
