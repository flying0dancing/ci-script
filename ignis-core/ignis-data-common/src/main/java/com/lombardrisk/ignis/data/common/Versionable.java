package com.lombardrisk.ignis.data.common;

public interface Versionable<T> extends Nameable {

    T getVersion();

    default String getVersionedName() {
        return versionKey(getName(), getVersion());
    }

    static <R> String versionKey(final String name, final R version) {
        return name + " v." + version;
    }
}
