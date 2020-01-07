package com.lombardrisk.ignis.design.server.configuration;

import com.lombardrisk.ignis.common.time.TimeSource;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ServerConfiguration {

    public TimeSource timeSource() {
        return new TimeSource(Clock.systemUTC());
    }
}
