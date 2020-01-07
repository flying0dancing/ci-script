package com.lombardrisk.ignis.server.util.spark;

import com.lombardrisk.ignis.server.batch.AppId;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang3.reflect.FieldUtils.readDeclaredField;

@Slf4j
@Getter
@Builder
public class StandaloneAppSession implements AppSession {

    private static final long DEFAULT_STOP_TIMEOUT = 3;

    private Process sparkApp;
    private final ProcessBuilder sparkAppBuilder;
    private final long clusterTimestamp = System.currentTimeMillis();

    @Override
    public Tuple2<AppStatus, FinalAppStatus> monitor() {
        runSparkApp();

        getRuntime().addShutdownHook(new Thread(sparkApp::destroyForcibly));

        return Tuple.of(toAppStatus(), toFinalAppStatus());
    }

    private void runSparkApp() {
        try {
            sparkApp = sparkAppBuilder.start();

            sparkApp.waitFor();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            sparkApp.destroyForcibly();

            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        try {
            sparkApp.destroy();
            sparkApp.waitFor(DEFAULT_STOP_TIMEOUT, SECONDS);
        } catch (InterruptedException e) {
            sparkApp.destroyForcibly();
        }
    }

    @Override
    public AppId getAppId() {
        return AppId.valueOf(clusterTimestamp, 0);
    }

    private static Integer findHandle(final Process process) {
        try {
            Long handle = (Long) readDeclaredField(process, "handle", true);
            return handle.intValue();
        } catch (@SuppressWarnings("squid:S1166") IllegalAccessException e) {
            return 0;
        }
    }

    private AppStatus toAppStatus() {
        if (sparkApp.isAlive()) {
            return AppStatus.RUNNING;
        }
        switch (sparkApp.exitValue()) {
            case 1:
                return AppStatus.FAILED;
            case 0:
            default:
                return AppStatus.FINISHED;
        }
    }

    private FinalAppStatus toFinalAppStatus() {
        switch (sparkApp.exitValue()) {
            case 1:
                return FinalAppStatus.FAILED;
            case 0:
            default:
                return FinalAppStatus.SUCCEEDED;
        }
    }
}
