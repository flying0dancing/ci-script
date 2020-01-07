package com.lombardrisk.ignis.spark.validation.transform;

import org.junit.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

public class FieldTypeTest {

    @Test
    public void testParse() {
        FieldType fieldTypeParser = FieldType.of(Type.LONG);
        assertThat(fieldTypeParser.parse(2L)).isEqualTo(2L);
        fieldTypeParser = FieldType.of(Type.BOOLEAN);
        assertThat(fieldTypeParser.parse("true")).isEqualTo(true);
        fieldTypeParser = FieldType.of(Type.INT);
        assertThat(fieldTypeParser.parse(2)).isEqualTo(2);

        fieldTypeParser = FieldType.of(Type.FLOAT);
        assertThat(fieldTypeParser.parse("0.2f")).isEqualTo(0.2f);

        fieldTypeParser = FieldType.of(Type.DOUBLE);
        assertThat(fieldTypeParser.parse("0.2d")).isEqualTo(0.2d);

        fieldTypeParser = FieldType.of(Type.STRING);
        assertThat(fieldTypeParser.parse("hello world")).isEqualTo("hello world");

        LocalDateTime localDateTime = LocalDateTime.of(2017, 6, 7, 10, 10, 10);
        fieldTypeParser = FieldType.of(Type.TIMESTAMP, "dd-MM-yyyy hh:mm:ss");
        Timestamp date1 = Timestamp.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        assertThat(fieldTypeParser.parse("07-06-2017 10:10:10")).isEqualTo(date1);

        fieldTypeParser = FieldType.of(Type.NULL);
        assertThat(fieldTypeParser.parse("NULL")).isEqualTo(null);
    }
}
