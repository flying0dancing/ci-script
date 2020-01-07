package com.lombardrisk.ignis.server.product.productconfig;

import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaPeriod;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.server.product.pipeline.PipelineImportService;
import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import com.lombardrisk.ignis.server.product.productconfig.model.ImportStatus;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfigFileContents;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductImportDiff;
import com.lombardrisk.ignis.server.product.table.TableService;
import com.lombardrisk.ignis.server.product.table.model.Table;
import com.lombardrisk.ignis.web.common.exception.RollbackTransactionException;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.SPACE;

@Slf4j
@AllArgsConstructor
public class ProductConfigImportEntityService {

    private static final String SPACED_SEP = ", ";
    private static final String COLON_SEP = " : ";

    private final ProductConfigRepository productConfigRepository;
    private final TimeSource timeSource;
    private final TableService tableService;
    private final PipelineImportService pipelineService;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = RollbackTransactionException.class)
    public Long saveProductConfig(
            final ProductImportDiff productImportDiff,
            final ProductConfigFileContents productConfigFileContents) throws RollbackTransactionException {

        Tuple2<Long, Set<Table>> idAndSchemas = saveProductConfig(productImportDiff);

        Validation<List<ErrorResponse>, List<Pipeline>> pipelineImportResult =
                pipelineService.importProductPipelines(
                        productConfigFileContents.getPipelineExports(), idAndSchemas._1(), idAndSchemas._2());

        if (pipelineImportResult.isInvalid()) {
            throw RollbackTransactionException.fromErrors(pipelineImportResult.getError());
        }

        return idAndSchemas._1();
    }

    @Transactional
    public void updateImportStatusError(final Long productConfigId) {
        final ProductConfig productConfig = productConfigRepository.getOne(productConfigId);
        if (productConfig != null && ImportStatus.IN_PROGRESS.equals(productConfig.getImportStatus())) {
            productConfig.setImportStatus(ImportStatus.ERROR);
            productConfigRepository.save(productConfig);
        }
    }

    private Tuple2<Long, Set<Table>> saveProductConfig(final ProductImportDiff productImportDiff) {
        logNewProduct(productImportDiff);

        ProductConfig newProduct = productConfigRepository.save(
                ProductConfig.builder()
                        .name(productImportDiff.getProductName())
                        .version(productImportDiff.getProductVersion())
                        .importStatus(ImportStatus.IN_PROGRESS)
                        .createdTime(timeSource.nowAsDate())
                        .build());

        Set<Table> newSchemas = productImportDiff.getNewSchemas();
        newSchemas.forEach(schema -> schema.setProductId(newProduct.getId()));

        List<Table> createdSchemas = new ArrayList<>(tableService.saveTables(newSchemas));

        Set<Table> newVersionedSchemas = productImportDiff.getNewVersionedSchemas();
        newVersionedSchemas.forEach(schema -> schema.setProductId(newProduct.getId()));
        createdSchemas.addAll(tableService.saveTables(newVersionedSchemas));

        for (Map.Entry<Table, SchemaPeriod> existingSchemaToNewPeriod
                : productImportDiff.getExistingSchemaToNewPeriod().entrySet()) {
            Long existingSchemaId = existingSchemaToNewPeriod.getKey().getId();
            SchemaPeriod newSchemaPeriod = existingSchemaToNewPeriod.getValue();

            tableService.updatePeriod(existingSchemaId, newSchemaPeriod);
        }

        return Tuple.of(
                newProduct.getId(),
                ImmutableSet.<Table>builder().addAll(createdSchemas)
                        .addAll(productImportDiff.getExistingSchemas())
                        .build());
    }

    private static void logNewProduct(final ProductImportDiff productImportDiff) {
        String existingSchemaPeriodUpdates =
                productImportDiff.getExistingSchemaToNewPeriod().entrySet()
                        .stream()
                        .map(ProductConfigImportEntityService::toNewSchemaPeriodLogEntry)
                        .collect(joining(SPACED_SEP));
        log.info("Updated schema periods [{}]", existingSchemaPeriodUpdates);

        String newSchemas = productImportDiff.getNewSchemas().stream()
                .map(Table::getVersionedName)
                .collect(joining(SPACED_SEP));

        String existingSchemas = productImportDiff.getExistingSchemas().stream()
                .map(Table::getVersionedName)
                .collect(joining(SPACED_SEP));

        String newVersionOfSchemas = productImportDiff.getNewVersionedSchemas().stream()
                .map(Table::getVersionedName)
                .collect(joining(SPACED_SEP));

        log.info(
                "Save product [{}] with existing schemas [{}] new schemas [{}] and new version of schemas [{}]",
                productImportDiff.getVersionedName(),
                existingSchemas,
                newSchemas,
                newVersionOfSchemas);
    }

    private static String toNewSchemaPeriodLogEntry(final Map.Entry<Table, SchemaPeriod> entry) {
        return entry.getKey().getVersionedName()
                + "(" + entry.getKey().getId() + ")"
                + SPACE + entry.getValue();
    }
}
