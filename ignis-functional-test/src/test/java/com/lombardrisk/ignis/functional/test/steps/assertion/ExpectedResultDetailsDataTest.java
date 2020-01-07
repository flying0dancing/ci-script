package com.lombardrisk.ignis.functional.test.steps.assertion;

import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.functional.test.assertions.expected.ExpectedResultDetailsData;
import org.junit.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class ExpectedResultDetailsDataTest {

    @Test
    public void buildResultsDetailData() {
        ExpectedResultDetailsData expectedResultDetailsData = ExpectedResultDetailsData.expectedData()
                .headers("HEADER1", "HEADER2", "HEADER3")
                .row(1, "a", 123L)
                .row(2, "b", 'B')
                .row(3, "c", new BigDecimal(9876))
                .build();

        assertThat(expectedResultDetailsData.getHeaders())
                .containsExactly("HEADER1", "HEADER2", "HEADER3");

        assertThat(expectedResultDetailsData.getData())
                .containsExactly(
                        ImmutableMap.<String, Object>builder()
                                .put("HEADER1", 1).put("HEADER2", "a").put("HEADER3", 123L).build(),
                        ImmutableMap.<String, Object>builder()
                                .put("HEADER1", 2).put("HEADER2", "b").put("HEADER3", 'B').build(),
                        ImmutableMap.<String, Object>builder()
                                .put("HEADER1", 3).put("HEADER2", "c").put("HEADER3", new BigDecimal(9876)).build());
    }

    @Test
    public void buildEmptyResultsDetailData() {
        ExpectedResultDetailsData expectedResultDetailsData = ExpectedResultDetailsData.expectedData()
                .headers("HEADER1", "HEADER2", "HEADER3")
                .build();

        assertThat(expectedResultDetailsData.getHeaders())
                .containsExactly("HEADER1", "HEADER2", "HEADER3");

        assertThat(expectedResultDetailsData.getData())
                .isEmpty();
    }
}