package com.lombardrisk.ignis.common.function;

import io.vavr.Function1;

public interface IsoMorphicFunction1<T,R> extends Function1<T,R> {

    Function1<R, T> inverse();

}
