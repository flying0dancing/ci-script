package com.lombardrisk.ignis.design.server.productconfig.schema;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SchemaTest {

    @Test
    public void new_Schema_ReturnsSchemaWithDefaults() {
        assertThat(new Schema().getLatest())
                .isTrue();
        assertThat(Schema.builder().build().getLatest())
                .isTrue();
    }
}