package com.lombardrisk.ignis.server.config.calendar;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.lombardrisk.ignis.common.json.LocalDateSerializer;
import com.lombardrisk.ignis.data.common.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CAL_HOLIDAY")
public class CalendarHoliday implements Identifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PRODUCT_NAME")
    private String product;

    @Column(name = "NAME")
    private String name;

    @JsonSerialize(using = LocalDateSerializer.class)
    @Column(name = "HOLIDAY_DATE")
    private LocalDate date;
}
