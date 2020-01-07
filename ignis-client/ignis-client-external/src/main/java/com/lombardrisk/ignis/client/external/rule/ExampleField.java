package com.lombardrisk.ignis.client.external.rule;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
public class ExampleField {

    public final Long id;

    public final String value;

    public final String error;
}
