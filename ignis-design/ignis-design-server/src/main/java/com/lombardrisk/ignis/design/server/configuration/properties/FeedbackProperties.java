package com.lombardrisk.ignis.design.server.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "feedback")
public class FeedbackProperties {

    private String teamsUrl;
}
