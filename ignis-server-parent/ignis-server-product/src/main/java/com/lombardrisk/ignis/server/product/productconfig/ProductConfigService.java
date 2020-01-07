package com.lombardrisk.ignis.server.product.productconfig;

import com.google.common.collect.Sets;
import com.lombardrisk.ignis.client.external.productconfig.view.ProductConfigView;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.service.JpaCRUDService;
import com.lombardrisk.ignis.server.product.productconfig.model.ImportStatus;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductSchemaPipeline;
import com.lombardrisk.ignis.server.product.productconfig.view.ProductSchemaPipelineListToProductConfigViewList;
import com.lombardrisk.ignis.server.product.productconfig.view.ProductConfigToProductConfigView;
import com.lombardrisk.ignis.server.product.table.TableService;
import com.lombardrisk.ignis.server.product.table.model.Table;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toSet;

@Slf4j
public class ProductConfigService implements JpaCRUDService<ProductConfig> {

    private static final LocalDate DEFAULT_START_DATE = LocalDate.of(1970, 1, 1);
    private final ProductConfigRepository productConfigRepository;
    private final Validator validator;
    private final TableService tableService;
    private final TimeSource timeSource;
    private final ProductConfigToProductConfigView
            productConfigToProductConfigView = new ProductConfigToProductConfigView();
    private final ProductSchemaPipelineListToProductConfigViewList
            productSchemaPipelineListToProductConfigViewList = new ProductSchemaPipelineListToProductConfigViewList();

    public ProductConfigService(
            final ProductConfigRepository productConfigRepository,
            final Validator validator,
            final TableService tableService,
            final TimeSource timeSource) {
        this.productConfigRepository = productConfigRepository;
        this.validator = validator;
        this.tableService = tableService;
        this.timeSource = timeSource;
    }

    @Override
    public String entityName() {
        return ProductConfig.class.getSimpleName();
    }

    @Override
    public JpaRepository<ProductConfig, Long> repository() {
        return productConfigRepository;
    }

    @Override
    public List<ProductConfig> findAll() {
        return repository().findAll(Sort.by(Order.asc("name"), Order.desc("version")));
    }

    public List<ProductSchemaPipeline> findAllWithoutFieldsRulesAndSteps() {
        return productConfigRepository.findAllWithoutFieldsRulesAndSteps();
    }

    public List<ProductConfigView> findAllViewsWithoutFieldsRulesAndSteps() {
        return productSchemaPipelineListToProductConfigViewList.apply(findAllWithoutFieldsRulesAndSteps());
    }

    @Transactional
    public Validation<CRUDFailure, ProductConfigView> findView(final Long id) {
        return findWithValidation(id)
                .map(productConfigToProductConfigView);
    }

    @Transactional
    public Either<List<ErrorResponse>, ProductConfig> saveProductConfig(final ProductConfig productConfig) {
        Set<ConstraintViolation<ProductConfig>> productConfigViolations = validator.validate(productConfig);
        if (!productConfigViolations.isEmpty()) {
            return Either.left(MapperUtils.map(
                    productConfigViolations,
                    ProductConfigErrorResponse::productConfigNotValid));
        }

        if (productConfigExists(productConfig)) {
            return Either.left(singletonList(ProductConfigErrorResponse.productConfigExists(productConfig.getName())));
        }

        Set<String> existingTables = existingTables(productConfig);
        if (!existingTables.isEmpty()) {
            return Either.left(MapperUtils.map(existingTables, ProductConfigErrorResponse::tableExists));
        }

        ProductConfig.ProductConfigBuilder productConfigBuilder = ProductConfig.builder()
                .name(productConfig.getName())
                .version(productConfig.getVersion())
                .createdTime(timeSource.nowAsDate());

        productConfig.getTables().forEach(ProductConfigService::setDefaultStartDate);
        List<Table> savedTables = tableService.saveTables(productConfig.getTables());

        productConfigBuilder.tables(new HashSet<>(savedTables));

        return Either.right(productConfigRepository.save(productConfigBuilder.build()));
    }

    public Validation<CRUDFailure, ProductConfig> findByName(final String productName) {
        return Option.ofOptional(productConfigRepository.findByName(productName))
                .toValid(CRUDFailure.cannotFind("Product").with("Name", productName).asFailure());
    }

    private static void setDefaultStartDate(final Table table) {
        table.setStartDate(DEFAULT_START_DATE);
    }

    @Transactional
    public void updateImportStatus(@NotNull final Long id, @NotNull final ImportStatus importStatus) {
        productConfigRepository.updateImportStatus(id, importStatus);
    }

    private boolean productConfigExists(final ProductConfig productConfig) {
        Optional<ProductConfig> existingProductWithName = productConfigRepository.findByName(productConfig.getName());
        return existingProductWithName.isPresent();
    }

    private Set<String> existingTables(final ProductConfig productConfig) {
        Set<String> existingTableNames = new HashSet<>(tableService.getAllTableNames());
        Set<String> newTableNames =
                productConfig.getTables().stream().map(Table::getPhysicalTableName).collect(toSet());

        return Sets.intersection(existingTableNames, newTableNames).immutableCopy();
    }
}
