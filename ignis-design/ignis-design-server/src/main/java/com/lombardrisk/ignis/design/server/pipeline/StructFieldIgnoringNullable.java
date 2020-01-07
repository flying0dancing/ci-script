package com.lombardrisk.ignis.design.server.pipeline;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.spark.sql.types.StructField;

@AllArgsConstructor
@Data
public class StructFieldIgnoringNullable {

    private final StructField structField;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StructFieldIgnoringNullable that = (StructFieldIgnoringNullable) o;
        return Objects.equal(structField.name(), that.structField.name())
                && Objects.equal(structField.dataType(), that.structField.dataType());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(structField.name(), structField.dataType());
    }
}
