package com.lombardrisk.ignis.design.server.productconfig.fixture;

import com.lombardrisk.ignis.data.common.fixtures.InMemoryRepository;
import com.lombardrisk.ignis.design.field.fixtures.FieldServiceFactory;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.api.ProductConfigRepository;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProductConfigRepositoryFixture extends InMemoryRepository<ProductConfig> implements ProductConfigRepository {

    private final SchemaRepositoryFixture schemaRepository;

    public ProductConfigRepositoryFixture(final SchemaRepositoryFixture schemaRepository) {
        this.schemaRepository = schemaRepository;
    }

    public static ProductConfigRepositoryFixture empty() {
        FieldServiceFactory fieldServiceFactory = FieldServiceFactory.create(x -> true);
        return new ProductConfigRepositoryFixture(new SchemaRepositoryFixture(fieldServiceFactory.getFieldService()));
    }

    @Override
    public Optional<ProductConfig> findByName(final String name) {
        return findAll().stream()
                .filter(productConfig -> productConfig.getName().equals(name))
                .map(this::addSchemasToProduct)
                .findFirst();
    }

    @Override
    public Optional<ProductConfig> findById(final long id) {
        return super.findById(id)
                .map(this::addSchemasToProduct);
    }

    @Override
    public List<ProductConfig> findAll() {
        return super.findAll().stream()
                .map(this::addSchemasToProduct)
                .collect(Collectors.toList());
    }

    private ProductConfig addSchemasToProduct(final ProductConfig product) {
        product.setTables(new LinkedHashSet<>());
        for (Schema schema : schemaRepository.findSchemas().collect(Collectors.toList())) {
            if (schema.getProductId().equals(product.getId())) {
                product.getTables().add(schema);
            }
        }
        return product;
    }

    @Override
    public List<ProductConfig> findAllById(final Iterable<Long> ids) {
        return super.findAllByIds(ids)
                .stream()
                .map(this::addSchemasToProduct)
                .collect(Collectors.toList());
    }
}
