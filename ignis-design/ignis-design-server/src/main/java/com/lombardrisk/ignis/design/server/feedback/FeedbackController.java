package com.lombardrisk.ignis.design.server.feedback;

import com.lombardrisk.ignis.client.design.path.design;
import com.lombardrisk.ignis.design.server.configuration.properties.FeedbackProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
public class FeedbackController {

    private final FeedbackProperties feedbackProperties;
    private final RestTemplate restTemplate;

    public FeedbackController(
            final FeedbackProperties feedbackProperties,
            final RestTemplate restTemplate) {
        this.feedbackProperties = feedbackProperties;
        this.restTemplate = restTemplate;
    }

    @PostMapping(path = design.api.v1.feedback.Submit, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity sendFeedback(@RequestBody final FeedbackMessage feedbackMessage) {
        return restTemplate.postForEntity(feedbackProperties.getTeamsUrl(), feedbackMessage, String.class);
    }

}
