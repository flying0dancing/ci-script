package com.lombardrisk.ignis.server.config.calendar;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.stream.Stream;

public interface ProductWorkingDayRepository extends JpaRepository<ProductWorkingDay, Long> {

    Stream<ProductWorkingDay> findAllByProduct(final String productName);

    void deleteAllByProduct(final String productName);
}
