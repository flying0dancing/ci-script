package com.lombardrisk.ignis.server.eraser.service;

import com.lombardrisk.ignis.server.dataset.DatasetJpaRepository;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.dataset.rule.ValidationResultsSummaryRepository;
import com.lombardrisk.ignis.server.product.table.TableRepository;
import com.lombardrisk.ignis.server.product.table.model.Table;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@AllArgsConstructor
public class SchemaService {

    private final TableRepository tableRepository;
    private final DatasetJpaRepository datasetJpaRepository;
    private final ValidationResultsSummaryRepository validationResultsSummaryRepository;
    private final JdbcTemplate phoenixJdbcTemplate;

    @Transactional
    public void deleteSchema(final Long schemaId) {
        Table schema = tableRepository.findById(schemaId)
                .orElseThrow(() -> new IllegalArgumentException("Could not find schema by id " + schemaId));

        deleteSchema(schema);
    }

    @Transactional
    public void deleteSchema(final Table schema) {
        for (Dataset dataset : datasetJpaRepository.findBySchemaId(schema.getId())) {
            log.info("Deleting all validation results summaries for Dataset {}", dataset.getId());
            validationResultsSummaryRepository.deleteAllByDataset(dataset);

            log.info("Deleting Dataset {}", dataset.getId());
            datasetJpaRepository.delete(dataset);
        }

        log.info("Deleting schema {}", schema);
        tableRepository.delete(schema);

        log.info("Delete physical table {}", schema.getPhysicalTableName());
        phoenixJdbcTemplate.execute("DROP TABLE IF EXISTS " + schema.getPhysicalTableName());
    }
}
