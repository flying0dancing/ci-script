package com.lombardrisk.ignis.common.assertions;

import io.vavr.control.Validation;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class VavrAssertTest {

    @Test
    public void assertPassed_Fail_ThrowsError() {
        Validation<String, Object> oops = Validation.invalid("OOPS");
        assertThatThrownBy(() -> VavrAssert.assertValid(oops))
                .hasMessage("Validation failed with error: OOPS");
    }

    @Test
    public void assertPassed_Pass_ReturnsContent() {
        Validation<String, Object> yay = Validation.valid("YAY");

        VavrAssert.assertValid(yay)
                .withResult("YAY");
    }

    @Test
    public void assertPassed_Extracting_ReturnsMappedContent() {
        Validation<String, Object> yay = Validation.valid("YAY");

        VavrAssert.assertValid(yay)
                .extracting(str -> str + " WHOA")
                .withResult("YAY WHOA");
    }

    @Test
    public void assertFailed_Pass_ThrowsError() {
        Validation<String, Object> yay = Validation.valid("YAY");
        assertThatThrownBy(() -> VavrAssert.assertFailed(yay))
                .hasMessage("Validation passed with result: YAY");
    }

    @Test
    public void assertFailed_Pass_ReturnsContent() {
        Validation<String, Object> oops = Validation.invalid("OOPS");

        VavrAssert.assertFailed(oops).withFailure("OOPS");
    }

    @Test
    public void assertCollectionFailure_Pass_ThrowsError() {
        Validation<Collection<String>, Object> yay = Validation.valid("YAY");
        assertThatThrownBy(() -> VavrAssert.assertCollectionFailure(yay))
                .hasMessage("Validation passed with result: YAY");
    }

    @Test
    public void assertCollectionFailure_Pass_ReturnsContent() {
        Validation<Collection<String>, Object> oops = Validation.invalid(Collections.singleton("OOPS"));

        VavrAssert.assertCollectionFailure(oops).withFailure("OOPS");
    }

    @Test
    public void assertCollectionFailure_ContainsOnly_ReturnsContent() {
        Validation<Collection<String>, Object> oops = Validation.invalid(Arrays.asList(
                "I've", "seen", "things", "you", "wouldn't", "believe"));

        VavrAssert.assertCollectionFailure(oops)
                .withOnlyFailures("I've", "seen", "things", "you", "wouldn't", "believe");
    }

    @Test
    public void assertCollectionFailure_Extracting_MapsContent() {
        Validation<Collection<String>, Object> oops = Validation.invalid(Arrays.asList(
                "Attack", "ships"));

        VavrAssert.assertCollectionFailure(oops)
                .extracting(s -> s.substring(0, 1))
                .withOnlyFailures("A", "s");
    }

    @Test
    public void assertFailure_Extracting_MapsContent() {
        Validation<String, Object> oops = Validation.invalid("Attack");

        VavrAssert.assertFailed(oops)
                .extracting(s -> s.substring(0, 1))
                .withFailure("A");
    }
}
