package com.lombardrisk.ignis.design.server.feedback;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
public class FeedbackMessage {

    private final String title;
    private final String text;
}
