package com.lombardrisk.ignis.server.controller.calendar;

import com.lombardrisk.ignis.client.external.path.api;
import com.lombardrisk.ignis.server.config.calendar.ProductWorkingDay;
import com.lombardrisk.ignis.server.config.calendar.ProductWorkingDayService;
import com.lombardrisk.ignis.web.common.response.FcrResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.util.List;

@RestController
@Slf4j
public class ProductWorkingDaysController {

    private final ProductWorkingDayService productWorkingDayService;

    public ProductWorkingDaysController(final ProductWorkingDayService productWorkingDayService) {
        this.productWorkingDayService = productWorkingDayService;
    }

    @GetMapping(value = api.external.v1.WorkingDays)
    public List<ProductWorkingDay> findAll() {
        return productWorkingDayService.findAll();
    }

    @ResponseBody
    @PutMapping(value = api.external.v1.productConfigs.byID.WorkingDays)
    public FcrResponse<List<ProductWorkingDay>> updateCalendar(
            @PathVariable(value = api.Params.ID) final Long productId,
            @RequestBody final List<DayOfWeek> workingDays) {

        log.info("Updating working days for product [{}]", productId);

        return productWorkingDayService.updateWorkingDays(productId, workingDays)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }
}
