package com.lombardrisk.ignis.server.util.spark;

import com.lombardrisk.ignis.server.batch.AppId;
import com.lombardrisk.ignis.server.fixtures.Populated;
import io.vavr.Tuple2;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.spark.deploy.yarn.Client;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class YarnAppSessionTest {

    @Test
    public void build_WithApplicationId_CreatesAppSessionWithTrackingUrl() {
        YarnAppSession yarnAppSession = Populated.appSession()
                .appId(14L, 10)
                .build();

        assertThat(yarnAppSession.getAppId())
                .extracting(AppId::getClusterTimestamp, AppId::getId)
                .containsSequence(14L, 10);
    }

    @Test
    public void close_StopsClient() {
        Client client = mock(Client.class);
        YarnAppSession yarnAppSession = Populated.appSession()
                .client(client)
                .build();

        yarnAppSession.close();

        verify(client).stop();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void monitor_ReturnsAppStatus() {
        Client client = mock(Client.class);

        when(client.monitorApplication(any(ApplicationId.class), anyBoolean(), anyBoolean()))
                .thenReturn(new scala.Tuple2<>(YarnApplicationState.SUBMITTED, FinalApplicationStatus.FAILED));

        YarnAppSession yarnAppSession = Populated.appSession()
                .client(client)
                .build();

        assertThat(yarnAppSession.monitor())
                .extracting(Tuple2::_1, Tuple2::_2)
                .containsSequence(AppStatus.SUBMITTED, FinalAppStatus.FAILED);
    }
}
