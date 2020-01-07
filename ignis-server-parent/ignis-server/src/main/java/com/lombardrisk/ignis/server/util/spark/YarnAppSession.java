package com.lombardrisk.ignis.server.util.spark;

import com.lombardrisk.ignis.server.batch.AppId;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.Value;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.spark.deploy.yarn.Client;

@Value
public final class YarnAppSession implements AppSession {

    private final AppId appId;
    private final Client client;

    private YarnAppSession(final YarnApplicationSessionBuilder builder) {
        this.appId = builder.appId;
        this.client = builder.client;
    }

    @Override
    public Tuple2<AppStatus, FinalAppStatus> monitor() {
        scala.Tuple2<YarnApplicationState, FinalApplicationStatus> monitorStatus =
                client.monitorApplication(
                        ApplicationId.newInstance(appId.getClusterTimestamp(), appId.getId()),
                        false,
                        true);

        return Tuple.of(
                AppStatus.valueOf(monitorStatus._1.name()),
                FinalAppStatus.valueOf(monitorStatus._2.name()));
    }

    @Override
    public void close() {
        client.stop();
    }

    public static YarnApplicationSessionBuilder builder() {
        return new YarnApplicationSessionBuilder();
    }

    public static class YarnApplicationSessionBuilder {

        private AppId appId;
        private Client client;

        public YarnApplicationSessionBuilder appId(final long clusterTimestamp, final int id) {
            this.appId = AppId.valueOf(clusterTimestamp, id);
            return this;
        }

        public YarnApplicationSessionBuilder client(final Client client) {
            this.client = client;
            return this;
        }

        public YarnAppSession build() {
            return new YarnAppSession(this);
        }
    }
}
