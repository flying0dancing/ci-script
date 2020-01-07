package com.lombardrisk.ignis.design.server.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class ImportCsvConfiguration {

    @Value("${import.csv.max-lines:100}")
    private Integer importMaxLines;
}
