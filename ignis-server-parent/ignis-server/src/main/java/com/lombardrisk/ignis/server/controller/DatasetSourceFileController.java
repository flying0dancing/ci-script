package com.lombardrisk.ignis.server.controller;

import com.lombardrisk.ignis.client.internal.path.api;
import com.lombardrisk.ignis.server.job.staging.file.DataSourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class DatasetSourceFileController {

    private final DataSourceService dataSourceService;

    public DatasetSourceFileController(final DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }

    @GetMapping(api.internal.datasets.source.Files)
    public List<String> getAllFileNames() throws IOException {
        return dataSourceService.findFileNames()
                .collect(Collectors.toList());
    }
}
