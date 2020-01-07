package com.lombardrisk.ignis.server.config.calendar;

import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigService;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import io.vavr.control.Validation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

@Slf4j
public class ProductWorkingDayService {

    private final ProductWorkingDayRepository productWorkingDayRepository;
    private final ProductConfigService productConfigService;

    public ProductWorkingDayService(
            final ProductWorkingDayRepository productWorkingDayRepository,
            final ProductConfigService productConfigService) {
        this.productWorkingDayRepository = productWorkingDayRepository;
        this.productConfigService = productConfigService;
    }

    public List<ProductWorkingDay> findAll() {
        return productWorkingDayRepository.findAll();
    }

    @Transactional
    public Stream<DayOfWeek> findByProductName(final String productName) {
        return productWorkingDayRepository.findAllByProduct(productName)
                .map(ProductWorkingDay::getDayOfWeek);
    }

    @Transactional
    public Validation<CRUDFailure, List<ProductWorkingDay>> updateWorkingDays(
            final Long productId,
            final List<DayOfWeek> productWorkingDays) {
        Validation<CRUDFailure, ProductConfig> product = productConfigService.findWithValidation(productId);
        if (product.isInvalid()) {
            log.error("Cannot find product for id [{}]", product);
            return Validation.invalid(product.getError());
        }

        Set<ProductWorkingDay> workingDays = productWorkingDays.stream()
                .map(dayOfWeek -> ProductWorkingDay.builder()
                        .dayOfWeek(dayOfWeek)
                        .product(product.get().getName())
                        .build())
                .collect(toCollection(HashSet::new));

        productWorkingDayRepository.deleteAllByProduct(product.get().getName());
        List<ProductWorkingDay> created = productWorkingDayRepository.saveAll(workingDays);
        return Validation.valid(created);
    }
}
