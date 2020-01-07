package com.lombardrisk.ignis.server.util.spark;

import com.lombardrisk.ignis.server.batch.AppId;
import io.vavr.Tuple2;

public interface AppSession extends AutoCloseable {

    AppId getAppId();

    Tuple2<AppStatus, FinalAppStatus> monitor();
}
