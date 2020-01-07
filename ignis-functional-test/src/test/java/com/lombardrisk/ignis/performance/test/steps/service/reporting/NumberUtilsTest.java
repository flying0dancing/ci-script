package com.lombardrisk.ignis.performance.test.steps.service.reporting;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("squid:S00100")
public class NumberUtilsTest {

    @Test
    public void scaleNumber_numberOver1k() {
        assertThat(NumberUtils.scaleNumber(1000L)).isEqualTo("1K");
    }

    @Test
    public void scaleNumber_numberOver10k() {
        assertThat(NumberUtils.scaleNumber(10000L)).isEqualTo("10K");
    }

    @Test
    public void scaleNumber_numberOver100k() {
        assertThat(NumberUtils.scaleNumber(100000L)).isEqualTo("100K");
    }

    @Test
    public void scaleNumber_numberOver999999() {
        assertThat(NumberUtils.scaleNumber(999999L)).isEqualTo("999K");
    }

    @Test
    public void scaleNumber_numberOver1m() {
        assertThat(NumberUtils.scaleNumber(1000000L)).isEqualTo("1M");
    }

    @Test
    public void scaleNumber_numberOver10m() {
        assertThat(NumberUtils.scaleNumber(10000000L)).isEqualTo("10M");
    }

    @Test
    public void scaleNumber_numberOver100m() {
        assertThat(NumberUtils.scaleNumber(100000000L)).isEqualTo("100M");
    }

    @Test
    public void scaleNumber_numberUnder1k() {
        assertThat(NumberUtils.scaleNumber(999L)).isEqualTo("999");
    }
}