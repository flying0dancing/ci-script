package com.lombardrisk.ignis.server.config.table;

import com.lombardrisk.ignis.server.controller.TableController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TableControllerConfiguration {

    private final TableServiceConfiguration tableServices;

    @Autowired
    public TableControllerConfiguration(
            final TableServiceConfiguration tableServices) {
        this.tableServices = tableServices;
    }

    @Bean
    public TableController tableController() {
        return new TableController(
                tableServices.tableService());
    }
}
