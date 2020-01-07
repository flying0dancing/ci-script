package com.lombardrisk.ignis.design.server.productconfig.schema.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.lombardrisk.ignis.common.json.LocalDateDeserializer;
import com.lombardrisk.ignis.common.json.LocalDateSerializer;
import com.lombardrisk.ignis.data.common.Versionable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CreateSchemaRequest implements Versionable<Integer> {

    private static final String REGEX_MESSAGE = "Physical Table Name has to start with at least 1 word character, "
            + "followed by 0 or more word characters or '_'";

    @NotNull
    @Size(min = 1, max = 31)
    @Pattern(regexp = "^[a-zA-Z]+[_\\w]*$", message = REGEX_MESSAGE)
    private String physicalTableName;

    @NotNull
    private String displayName;

    @NotNull
    private Integer majorVersion;

    @NotNull
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate startDate;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate endDate;

    @Override
    public Integer getVersion() {
        return majorVersion;
    }

    @Override
    public String getName() {
        return displayName;
    }
}
