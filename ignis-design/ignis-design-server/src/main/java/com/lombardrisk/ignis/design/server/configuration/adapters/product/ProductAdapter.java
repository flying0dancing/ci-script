package com.lombardrisk.ignis.design.server.configuration.adapters.product;

import com.lombardrisk.ignis.design.server.jpa.ProductConfigJpaRepository;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.api.ProductConfigRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
public class ProductAdapter implements ProductConfigRepository {

    private final ProductConfigJpaRepository productConfigRepository;

    public ProductAdapter(final ProductConfigJpaRepository productConfigRepository) {
        this.productConfigRepository = productConfigRepository;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public <T extends ProductConfig> T save(final T productConfig) {
        return productConfigRepository.save(productConfig);
    }

    @Override
    public boolean existsById(final Long id) {
        return productConfigRepository.existsById(id);
    }

    @Override
    public Optional<ProductConfig> findByName(final String productConfigName) {
        return productConfigRepository.findByName(productConfigName);
    }

    @Override
    public Optional<ProductConfig> findById(final long id) {
        return productConfigRepository.findById(id);
    }

    @Override
    public List<ProductConfig> findAllById(final Iterable<Long> ids) {
        return productConfigRepository.findAllById(ids);
    }

    @Override
    public List<ProductConfig> findAll() {
        return productConfigRepository.findAll();
    }

    @Override
    public List<ProductConfig> findAllByIds(final Iterable<Long> ids) {
        return productConfigRepository.findAllById(ids);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteById(final Long id) {
        productConfigRepository.deleteById(id);
    }
}
