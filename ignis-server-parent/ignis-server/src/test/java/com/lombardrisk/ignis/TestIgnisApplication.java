package com.lombardrisk.ignis;

import com.lombardrisk.ignis.server.config.ApplicationConf;
import com.lombardrisk.ignis.test.config.MockBeanConfiguration;
import com.lombardrisk.ignis.web.common.exception.GlobalExceptionHandler;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.boot.actuate.autoconfigure.metrics.jdbc.DataSourcePoolMetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import java.nio.file.Paths;

@SpringBootApplication(exclude = { DataSourcePoolMetricsAutoConfiguration.class })
@Import(MockBeanConfiguration.class)
@ComponentScan(basePackageClasses = { ApplicationConf.class, GlobalExceptionHandler.class }, lazyInit = true)
public class TestIgnisApplication {

    static {
        if (SystemUtils.IS_OS_WINDOWS) {
            System.setProperty(
                    "hadoop.home.dir",
                    Paths.get("../ignis-core/ignis-common/src/test/resources/hadoop")
                            .toAbsolutePath()
                            .toString());
        }
    }
}
