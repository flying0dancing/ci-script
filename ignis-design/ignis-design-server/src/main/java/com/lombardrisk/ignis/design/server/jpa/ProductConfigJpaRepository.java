package com.lombardrisk.ignis.design.server.jpa;

import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductConfigJpaRepository extends JpaRepository<ProductConfig, Long> {

    Optional<ProductConfig> findByName(String name);
}
