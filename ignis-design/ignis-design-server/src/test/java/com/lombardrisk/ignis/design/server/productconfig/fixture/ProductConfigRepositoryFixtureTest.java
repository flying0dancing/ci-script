package com.lombardrisk.ignis.design.server.productconfig.fixture;

import com.lombardrisk.ignis.design.server.fixtures.Design;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.api.ProductConfigRepository;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductConfigRepositoryFixtureTest {

    private ProductConfigRepository productConfigRepository;

    @Before
    public void setUp() {
        productConfigRepository = ProductConfigRepositoryFixture.empty();
    }

    @Test
    public void save_AddsId() {
        ProductConfig productConfig = productConfigRepository.save(Design.Populated.productConfig().id(null).build());
        assertThat(productConfig.getId())
                .isEqualTo(1L);
    }
}