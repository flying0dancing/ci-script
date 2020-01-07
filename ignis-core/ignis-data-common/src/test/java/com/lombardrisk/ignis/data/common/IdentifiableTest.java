package com.lombardrisk.ignis.data.common;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IdentifiableTest {

    @Test
    public void toIdentifiable_returnsIdentifiableWithId() {
        Identifiable identifiable = new TestImpl(180L).toIdentifiable();

        assertThat(identifiable.getId())
                .isEqualTo(180L);
    }

    @Test
    public void toIdentifiable_returnsIdentifiableInstance() {
        Identifiable identifiable = new TestImpl(180L).toIdentifiable();

        assertThat(identifiable.getId())
                .isEqualTo(180L);

        assertThat(identifiable)
                .isNotInstanceOf(TestImpl.class)
                .isInstanceOf(Identifiable.class);
    }

    private static final class TestImpl implements Identifiable {

        private final Long id;

        private TestImpl(final Long id) {
            this.id = id;
        }

        @Override
        public Long getId() {
            return id;
        }
    }
}