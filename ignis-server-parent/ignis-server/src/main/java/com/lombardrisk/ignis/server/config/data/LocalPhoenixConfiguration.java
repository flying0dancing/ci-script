package com.lombardrisk.ignis.server.config.data;

import com.lombardrisk.ignis.server.annotation.DataSourceQualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import static com.lombardrisk.ignis.server.annotation.DataSourceQualifier.Target.PHOENIX;

@Configuration
@ConditionalOnProperty(name = "spark-defaults.conf.debug.mode", havingValue = "true")
public class LocalPhoenixConfiguration {

    private final DataSource phoenixDatasource;
    private final Resource phoenixSql;

    public LocalPhoenixConfiguration(
            @DataSourceQualifier(PHOENIX) final DataSource phoenixDatasource,
            @Value("${phoenix.datasource.schema.sql}") final Resource phoenixSql) {
        this.phoenixDatasource = phoenixDatasource;
        this.phoenixSql = phoenixSql;
    }

    @PostConstruct
    public void setupSchema() {
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.addScript(phoenixSql);

        databasePopulator.execute(phoenixDatasource);
    }
}
