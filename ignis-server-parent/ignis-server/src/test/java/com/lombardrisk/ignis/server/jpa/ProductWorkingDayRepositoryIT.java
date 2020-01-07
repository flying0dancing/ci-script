package com.lombardrisk.ignis.server.jpa;

import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.config.calendar.ProductWorkingDay;
import com.lombardrisk.ignis.server.config.calendar.ProductWorkingDayRepository;
import com.lombardrisk.ignis.server.config.fixture.ServerConfig;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigRepository;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class ProductWorkingDayRepositoryIT {

    @Autowired
    private ProductConfigRepository productConfigRepository;
    @Autowired
    private ProductWorkingDayRepository productWorkingDayRepository;
    @Autowired
    private EntityManager entityManager;

    @Rule
    public final JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void saveAndRetrieve() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig("PRA").build());

        ProductWorkingDay productWorkingDay =
                productWorkingDayRepository.save(ServerConfig.Populated.productWorkingDay()
                        .product("PRA")
                        .dayOfWeek(DayOfWeek.MONDAY)
                        .build());

        entityManager.flush();
        entityManager.clear();

        ProductWorkingDay retrieved = productWorkingDayRepository.getOne(productWorkingDay.getId());
        soft.assertThat(retrieved.getProduct())
                .isEqualTo("PRA");
        soft.assertThat(retrieved.getDayOfWeek())
                .isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    public void findByProduct_ReturnsOnlyWorkingDaysForProduct() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig("PRA").build());

        productWorkingDayRepository.saveAll(Arrays.asList(
                ServerConfig.Populated.productWorkingDay()
                        .product("PRA")
                        .dayOfWeek(DayOfWeek.MONDAY)
                        .build(),
                ServerConfig.Populated.productWorkingDay()
                        .product("PRA")
                        .dayOfWeek(DayOfWeek.TUESDAY)
                        .build(),
                ServerConfig.Populated.productWorkingDay()
                        .product("APRA")
                        .dayOfWeek(DayOfWeek.MONDAY)
                        .build()));

        entityManager.flush();
        entityManager.clear();

        assertThat(productWorkingDayRepository.findAllByProduct("PRA").collect(Collectors.toList()))
                .extracting(ProductWorkingDay::getProduct, ProductWorkingDay::getDayOfWeek)
                .containsExactlyInAnyOrder(tuple("PRA", DayOfWeek.MONDAY), tuple("PRA", DayOfWeek.TUESDAY));
    }
}
