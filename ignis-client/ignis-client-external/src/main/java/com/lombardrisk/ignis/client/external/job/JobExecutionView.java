package com.lombardrisk.ignis.client.external.job;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.lombardrisk.ignis.common.json.ZoneLocalDateTimeDeserializer;
import com.lombardrisk.ignis.common.json.ZonedLocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.ZonedDateTime;
import java.util.List;

@SuppressWarnings("squid:UndocumentedApi")
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
@Getter
@Builder
@EqualsAndHashCode
@ToString
public class JobExecutionView {

    private final long id;

    private final String name;

    private final JobType serviceRequestType;

    private final JobStatus status;

    private final ExternalExitStatus exitCode;

    private final List<String> errors;

    @JsonSerialize(using = ZonedLocalDateTimeSerializer.class)
    @JsonDeserialize(using = ZoneLocalDateTimeDeserializer.class)
    private final ZonedDateTime startTime;

    @JsonSerialize(using = ZonedLocalDateTimeSerializer.class)
    @JsonDeserialize(using = ZoneLocalDateTimeDeserializer.class)
    private final ZonedDateTime endTime;

    private final String createUser;

    private final String yarnApplicationTrackingUrl;

    private final String requestMessage;
}
