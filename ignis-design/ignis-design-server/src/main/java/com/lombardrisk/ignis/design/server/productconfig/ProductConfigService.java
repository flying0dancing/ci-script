package com.lombardrisk.ignis.design.server.productconfig;

import com.lombardrisk.ignis.client.design.productconfig.NewProductConfigRequest;
import com.lombardrisk.ignis.client.design.productconfig.ProductConfigDto;
import com.lombardrisk.ignis.client.design.productconfig.UpdateProductConfig;
import com.lombardrisk.ignis.client.design.schema.NewSchemaVersionRequest;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.service.CRUDService;
import com.lombardrisk.ignis.design.server.productconfig.api.ProductConfigRepository;
import com.lombardrisk.ignis.design.server.productconfig.api.ProductPipelineRepository;
import com.lombardrisk.ignis.design.server.productconfig.converter.ProductConfigConverter;
import com.lombardrisk.ignis.design.server.productconfig.converter.request.SchemaRequestConverter;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import com.lombardrisk.ignis.design.server.productconfig.schema.SchemaService;
import com.lombardrisk.ignis.design.server.productconfig.schema.request.CreateSchemaRequest;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@Service
@Slf4j
@AllArgsConstructor
public class ProductConfigService implements CRUDService<ProductConfig> {

    private final SchemaService schemaService;
    private final ProductConfigRepository productConfigRepository;
    private final ProductConfigConverter productConfigConverter;
    private final SchemaRequestConverter schemaRequestConverter;
    private final ProductPipelineRepository productPipelineRepository;

    @Override
    public String entityName() {
        return ProductConfig.class.getSimpleName();
    }

    @Override
    public Option<ProductConfig> findById(final long id) {
        return Option.ofOptional(productConfigRepository.findById(id));
    }

    @Override
    public List<ProductConfig> findAllByIds(final Iterable<Long> ids) {
        return productConfigRepository.findAllByIds(ids);
    }

    @Override
    public List<ProductConfig> findAll() {
        return productConfigRepository.findAll();
    }

    @Transactional
    public List<ProductConfigDto> findAllProductConfigs() {
        return findAll().stream()
                .map(productConfigConverter)
                .collect(toList());
    }

    public Validation<CRUDFailure, ProductConfigDto> findOne(final long id) {
        return findWithValidation(id)
                .map(productConfigConverter);
    }

    @Transactional
    public Validation<CRUDFailure, Identifiable> deleteById(final long id) {
        if (!productConfigRepository.existsById(id)) {
            return Validation.invalid(notFound(id));
        }

        productPipelineRepository.deleteAllByProductId(id);
        productConfigRepository.deleteById(id);
        return Validation.valid(() -> id);
    }

    @Transactional
    public Validation<ErrorResponse, ProductConfig> createProductConfig(final NewProductConfigRequest productConfigRequest) {
        Optional<ErrorResponse> existingProductFailure = checkProductIsUnique(productConfigRequest.getName());
        if (existingProductFailure.isPresent()) {
            return Validation.invalid(existingProductFailure.get());
        }

        ProductConfig save = saveProductConfig(productConfigRequest);
        return Validation.valid(save);
    }

    ProductConfig saveProductConfig(final NewProductConfigRequest productConfigRequest) {
        return productConfigRepository.save(ProductConfig.builder()
                .name(productConfigRequest.getName())
                .version(productConfigRequest.getVersion())
                .build());
    }

    private Optional<ErrorResponse> checkProductIsUnique(final String productName) {
        return productConfigRepository.findByName(productName)
                .map(noop -> ErrorResponse.valueOf("Product '" + productName + "' already exists", "name"));
    }

    @Transactional
    public Validation<List<CRUDFailure>, Schema> createNewSchemaOnProduct(
            final Long productId,
            final CreateSchemaRequest createSchemaRequest) {
        Validation<CRUDFailure, ProductConfig> findProductResult = findWithValidation(productId);
        if (findProductResult.isInvalid()) {
            return Validation.invalid(singletonList(findProductResult.getError()));
        }

        ProductConfig productConfig = findProductResult.get();
        Schema convertedSchema = schemaRequestConverter.apply(productId, createSchemaRequest);

        Validation<List<CRUDFailure>, Schema> createSchemaResult = schemaService.validateAndCreateNew(convertedSchema);
        if (createSchemaResult.isInvalid()) {
            return createSchemaResult;
        }

        Schema schema = addSchemaToProduct(productConfig, createSchemaResult.get());
        return Validation.valid(schema);
    }

    private Schema addSchemaToProduct(final ProductConfig productConfig, final Schema schema) {
        if (productConfig.getTables() == null) {
            productConfig.setTables(new HashSet<>());
        }

        productConfig.getTables().add(schema);

        productConfigRepository.save(productConfig);
        return schema;
    }

    @Override
    public ProductConfig delete(final ProductConfig productConfig) {
        throw new UnsupportedOperationException("Current jpa mappings only allow delete by id");
    }

    @Transactional
    public Validation<CRUDFailure, Identifiable> removeSchemaFromProduct(final Long id, final Long schemaId) {
        if (!productConfigRepository.existsById(id)) {
            return Validation.invalid(notFound(id));
        }

        return schemaService.deleteByProductIdAndId(id, schemaId);
    }

    @Transactional
    public Validation<CRUDFailure, ProductConfigDto> updateProduct(
            final Long id,
            final UpdateProductConfig updateProductConfig) {
        Validation<CRUDFailure, ProductConfig> findProductResult = findWithValidation(id);
        if (findProductResult.isInvalid()) {
            return Validation.invalid(findProductResult.getError());
        }

        ProductConfig productConfig = findProductResult.get();
        Optional<String> versionOption = updateProductConfig.getVersion();
        versionOption.ifPresent(productConfig::setVersion);

        Optional<String> nameOption = updateProductConfig.getName();
        nameOption.ifPresent(productConfig::setName);

        ProductConfig updated = productConfigRepository.save(productConfig);
        return Validation.valid(productConfigConverter.apply(updated));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Validation<CRUDFailure, Schema> createNewSchemaVersion(
            final Long productConfigId,
            final Long schemaId,
            final NewSchemaVersionRequest newSchemaVersionRequest) {
        Validation<CRUDFailure, ProductConfig> findProduct = findWithValidation(productConfigId);
        if (findProduct.isInvalid()) {
            return Validation.invalid(notFound(productConfigId));
        }

        return schemaService.createNextVersion(findProduct.get(), schemaId, newSchemaVersionRequest);
    }
}
