package com.lombardrisk.ignis.client.external;

import com.lombardrisk.ignis.client.core.config.ClientConfig;
import com.lombardrisk.ignis.client.core.config.ConnectionProperties;
import com.lombardrisk.ignis.client.core.response.ErrorMapper;
import com.lombardrisk.ignis.client.core.response.IgnisResponseHandler;
import com.lombardrisk.ignis.client.external.dataset.DatasetClient;
import com.lombardrisk.ignis.client.external.dataset.DatasetService;

public class IgnisClientConfig {

    private final ClientConfig clientConfig;
    private final ErrorMapper errorMapper;

    public IgnisClientConfig(final ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        this.errorMapper = new ErrorMapper(clientConfig.getObjectMapper());
    }

    public DatasetService datasetService(final ConnectionProperties connectionProperties) {
        DatasetClient client = clientConfig.retrofit(connectionProperties)
                .create(DatasetClient.class);

        return new DatasetService(client, new IgnisResponseHandler(errorMapper));
    }
}
