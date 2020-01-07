package com.lombardrisk.ignis.spark.validation.transform;

import com.lombardrisk.ignis.common.jexl.JexlEngineFactory;
import com.lombardrisk.ignis.common.lang.ErrorMessage;
import com.lombardrisk.ignis.spark.util.DatasetRow;
import io.vavr.Function2;
import io.vavr.control.Try;
import lombok.Getter;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Row;
import scala.Tuple2;

/**
 * Takes in a transformation and returns a result object along with the original row.
 *
 * @param <T> The output of the JexlTransformation
 * @param <R> Object to wrap the output of the function, including potential error message string
 */
@Getter
public class JexlFunction<T, R extends Result<T>> implements MapFunction<Row, Tuple2<R, Row>> {

    private static final long serialVersionUID = 3255509384121870389L;
    private static final JexlEngine JEXL_ENGINE = JexlEngineFactory.jexlEngine().create();

    private final JexlTransformation<T> jexlTransformation;
    private final Function2<T, String, R> resultFunction;

    public JexlFunction(
            final JexlTransformation<T> jexlTransformation,
            final Function2<T, String, R> resultFunction) {
        this.jexlTransformation = jexlTransformation;
        this.resultFunction = resultFunction;
    }

    @Override
    public Tuple2<R, Row> call(final Row row) {
        return Try.of(() -> resolve(jexlTransformation, row))
                .toEither()
                .fold(
                        throwable -> onError(row, throwable),
                        value -> onSuccess(row, value));
    }

    private Tuple2<R, Row> onSuccess(final Row row, final T value) {
        R apply = resultFunction.apply(value, null);
        return Tuple2.apply(apply, row);
    }

    private Tuple2<R, Row> onError(final Row row, final Throwable throwable) {
        R tResult = resultFunction.apply(null, ErrorMessage.of(throwable).getMessage());
        return Tuple2.apply(tResult, row);
    }

    private T resolve(final JexlTransformation<T> field, final Row r) {
        Object result = evaluate(field.getName(), new DatasetRow(r).asMap());
        return field.parse(result);
    }

    private Object evaluate(final String expression, final java.util.Map<String, Object> rowMap) {
        JexlContext jexlContext = new MapContext(rowMap);
        return JEXL_ENGINE.createScript(expression).execute(jexlContext);
    }
}
