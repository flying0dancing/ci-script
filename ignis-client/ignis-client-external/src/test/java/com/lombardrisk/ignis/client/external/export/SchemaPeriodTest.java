package com.lombardrisk.ignis.client.external.export;

import com.lombardrisk.ignis.client.external.productconfig.export.SchemaPeriod;
import org.junit.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SchemaPeriodTest {

    @Test
    public void between_StartAndEndDate_ReturnsSchemaPeriod() {
        assertThat(
                SchemaPeriod.between(LocalDate.of(2000, 3, 3), LocalDate.of(2220, 3, 3))
        )
                .extracting(SchemaPeriod::getStartDate, SchemaPeriod::getEndDate)
                .containsExactly(LocalDate.of(2000, 3, 3), LocalDate.of(2220, 3, 3));
    }

    @Test
    public void between_StartDateAndNullEndDate_ReturnsSchemaPeriod() {
        assertThat(
                SchemaPeriod.between(LocalDate.of(2000, 3, 3), null)
        )
                .extracting(SchemaPeriod::getStartDate, SchemaPeriod::getEndDate)
                .containsExactly(LocalDate.of(2000, 3, 3), null);
    }

    @Test
    public void between_StartDateAfterEndDate_ThrowsException() {
        assertThatThrownBy(() ->
                SchemaPeriod.between(LocalDate.of(2000, 3, 3), LocalDate.of(1990, 3, 3))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("start date")
                .hasMessageContaining("must be before end date");
    }

    @Test
    public void between_NullStartDate_ThrowsExceptions() {
        assertThatThrownBy(() ->
                SchemaPeriod.between(null, LocalDate.of(2000, 3, 3))
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void max_ReturnsMinStartDateAndNullEndDate() {
        assertThat(SchemaPeriod.max())
                .extracting(SchemaPeriod::getStartDate, SchemaPeriod::getEndDate)
                .containsExactly(LocalDate.of(1970, 1, 1), null);
    }

    @Test
    public void isDifferent_SameStartAndEndDates_ReturnsFalse() {
        assertThat(
                SchemaPeriod.max().isDifferent(SchemaPeriod.max())
        ).isFalse();
    }

    @Test
    public void isDifferent_Null_ThrowsException() {
        assertThatThrownBy(() ->
                SchemaPeriod.max()
                        .isDifferent(null)
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void isDifferent_SameStartDatesAndDiffEndDates_ReturnsTrue() {
        assertThat(
                SchemaPeriod.between(LocalDate.of(2000, 1, 1), LocalDate.of(2200, 1, 1))
                        .isDifferent(
                                SchemaPeriod.between(LocalDate.of(2000, 1, 1), LocalDate.of(2020, 1, 1)))
        ).isTrue();
    }

    @Test
    public void isDifferent_SameStartDatesAndNullOtherEndDate_ReturnsTrue() {
        assertThat(
                SchemaPeriod.between(LocalDate.of(2000, 1, 1), LocalDate.of(2200, 1, 1))
                        .isDifferent(
                                SchemaPeriod.between(LocalDate.of(2000, 1, 1), null))
        ).isTrue();
    }

    @Test
    public void isDifferent_SameStartDatesAndNullEndDate_ReturnsTrue() {
        assertThat(
                SchemaPeriod.between(LocalDate.of(2000, 1, 1), null)
                        .isDifferent(
                                SchemaPeriod.between(LocalDate.of(2000, 1, 1), LocalDate.of(2003, 1, 1)))
        ).isTrue();
    }

    @Test
    public void isDifferent_DiffStartDatesAndSameEndDates_ReturnsTrue() {
        assertThat(
                SchemaPeriod.between(LocalDate.of(1999, 1, 1), null)
                        .isDifferent(
                                SchemaPeriod.between(LocalDate.of(2000, 1, 1), null))
        ).isTrue();
    }

    @Test
    public void toString_ReturnsRange() {
        assertThat(
                SchemaPeriod.between(LocalDate.of(1999, 9, 11), LocalDate.of(2001, 12, 13))
                        .toString()
        ).contains("1999", "09", "11", "2001", "12", "13");
    }

    @Test
    public void toString_NullEndDate_ReturnsRange() {
        assertThat(
                SchemaPeriod.between(LocalDate.of(1999, 9, 11), null)
                        .toString()
        ).contains("1999", "09", "11");
    }
}