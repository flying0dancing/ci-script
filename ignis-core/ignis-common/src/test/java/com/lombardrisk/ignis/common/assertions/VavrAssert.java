package com.lombardrisk.ignis.common.assertions;

import com.lombardrisk.ignis.common.MapperUtils;
import io.vavr.control.Validation;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@UtilityClass
public class VavrAssert {

    public static <FAIL, PASS> VavrAssertPass<PASS> assertValid(final Validation<FAIL, PASS> validation) {
        if (validation.isInvalid()) {
            return fail("Validation failed with error: " + validation.getError());
        }

        return new VavrAssertPass<>(validation.get());
    }

    public static <FAIL, PASS> VavrAssertFailure<FAIL> assertFailed(final Validation<FAIL, PASS> validation) {
        if (validation.isInvalid()) {
            return new VavrAssertFailure<>(validation.getError());
        }

        return fail("Validation passed with result: " + validation.get());
    }

    public static <T extends Collection<FAIL>, FAIL, PASS> VavrAssertCollectionFailure<FAIL> assertCollectionFailure(
            final Validation<T, PASS> validation) {
        if (validation.isInvalid()) {
            return new VavrAssertCollectionFailure<>(validation.getError());
        }

        return fail("Validation passed with result: " + validation.get());
    }

    @Getter
    public static class VavrAssertFailure<FAIL> {

        private final FAIL validation;

        public VavrAssertFailure(final FAIL validation) {
            this.validation = validation;
        }

        public VavrAssertFailure<FAIL> withFailure(final FAIL failure) {
            assertThat(validation)
                    .isEqualTo(failure);
            return this;
        }

        public <T> VavrAssertFailure<T> extracting(final Function<FAIL, T> mapper) {
            return new VavrAssertFailure<>(mapper.apply(validation));
        }
    }

    @Getter
    public static class VavrAssertPass<PASS> {

        private final PASS result;

        public VavrAssertPass(final PASS validation) {
            this.result = validation;
        }

        public VavrAssertPass<PASS> withResult(final PASS result) {
            assertThat(this.result)
                    .isEqualTo(result);
            return this;
        }

        public <T> VavrAssertPass<T> extracting(final Function<PASS, T> mapper) {
            return new VavrAssertPass<>(mapper.apply(result));
        }
    }

    public static class VavrAssertCollectionFailure<FAIL> {

        private final Collection<FAIL> validation;

        public VavrAssertCollectionFailure(final Collection<FAIL> validation) {
            this.validation = validation;
        }

        public VavrAssertCollectionFailure<FAIL> withFailure(final FAIL... failure) {
            assertThat(validation)
                    .contains(failure);
            return this;
        }

        public VavrAssertCollectionFailure<FAIL> withOnlyFailures(final FAIL... failure) {
            assertThat(validation)
                    .containsOnly(failure);
            return this;
        }


        public <T> VavrAssertCollectionFailure<T> extracting(final Function<FAIL, T> mapper) {
            return new VavrAssertCollectionFailure<>(MapperUtils.map(validation, mapper::apply));
        }
    }
}
