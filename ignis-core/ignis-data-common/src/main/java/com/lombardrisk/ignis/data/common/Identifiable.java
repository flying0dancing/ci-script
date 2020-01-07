package com.lombardrisk.ignis.data.common;

public interface Identifiable {

    Long getId();

    default void setId(final Long id) {

    }

    static Identifiable fromId(final Long id) {
        return () -> id;
    }

    default Identifiable toIdentifiable() {
        Long thisId = getId();
        return () -> thisId;
    }
}
