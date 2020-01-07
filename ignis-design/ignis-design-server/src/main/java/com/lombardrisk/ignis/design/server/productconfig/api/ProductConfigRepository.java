package com.lombardrisk.ignis.design.server.productconfig.api;

import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;

import java.util.List;
import java.util.Optional;

public interface ProductConfigRepository {

    <T extends ProductConfig> T save(T productConfig);

    boolean existsById(Long id);

    Optional<ProductConfig> findByName(String productConfigName);

    Optional<ProductConfig> findById(long id);

    List<ProductConfig> findAllById(Iterable<Long> ids);

    List<ProductConfig> findAll();

    List<ProductConfig> findAllByIds(Iterable<Long> ids);

    void deleteById(Long id);
}
