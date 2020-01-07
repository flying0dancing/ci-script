package com.lombardrisk.ignis.performance.test.config.properties;

import com.lombardrisk.ignis.functional.test.config.properties.DirectoryUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.nio.file.Path;

@ConfigurationProperties(prefix = "performance")
@Getter
@Setter
public class PerformanceTestProperties {
    private Path reportsPath;
    private Path trendReportsPath;

    @PostConstruct
    public void init() {
        DirectoryUtils.makeDirectory(reportsPath);
        DirectoryUtils.makeDirectory(trendReportsPath);
    }
}
