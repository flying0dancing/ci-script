package com.lombardrisk.ignis.server.config.drillback;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class ExportConfiguration {

    @Value("${export.dataset.page-size:50}")
    private Integer exportDatasetPageSize;

    @Value("${export.dataset.max-records:7000}")
    private Integer exportDatasetLimit;
}
