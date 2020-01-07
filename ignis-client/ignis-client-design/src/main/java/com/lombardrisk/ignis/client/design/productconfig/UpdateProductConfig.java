package com.lombardrisk.ignis.client.design.productconfig;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Optional;

@EqualsAndHashCode
@Builder
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
@ToString
public class UpdateProductConfig {

    @JsonProperty("name")
    private final String name;

    @JsonProperty("version")
    private final String version;

    @JsonIgnore
    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    @JsonIgnore
    public Optional<String> getVersion() {
        return Optional.ofNullable(version);
    }
}
