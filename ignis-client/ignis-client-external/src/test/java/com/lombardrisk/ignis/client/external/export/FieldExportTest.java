package com.lombardrisk.ignis.client.external.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FieldExportTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void marshallToJson_StringFieldView_MarshalledSuccessfully() throws Exception {
        FieldExport.StringFieldExport stringFieldView = FieldExport.StringFieldExport.builder()
                .id(100L)
                .name("name")
                .nullable(false)
                .maxLength(100)
                .minLength(90)
                .regularExpression("regex")
                .build();

        String fieldAsString = objectMapper.writeValueAsString(stringFieldView);

        assertThat(objectMapper.readValue(fieldAsString, FieldExport.class))
                .isEqualToComparingFieldByField(stringFieldView);
    }

    @Test
    public void marshallToJson_DecimalFieldView_MarshalledSuccessfully() throws Exception {
        FieldExport.DecimalFieldExport decimalFieldView = FieldExport.DecimalFieldExport.builder()
                .id(100L)
                .name("name")
                .nullable(true)
                .precision(10920)
                .scale(1092139123)
                .build();

        String fieldAsString = objectMapper.writeValueAsString(decimalFieldView);

        assertThat(objectMapper.readValue(fieldAsString, FieldExport.class))
                .isEqualToComparingFieldByField(decimalFieldView);
    }

    @Test
    public void marshallToJson_DoubleFieldView_MarshalledSuccessfully() throws Exception {
        FieldExport.DoubleFieldExport doubleFieldView = FieldExport.DoubleFieldExport.builder()
                .id(1123L)
                .name("doubleField")
                .nullable(true)
                .build();

        String fieldAsString = objectMapper.writeValueAsString(doubleFieldView);

        assertThat(objectMapper.readValue(fieldAsString, FieldExport.class))
                .isEqualToComparingFieldByField(doubleFieldView);
    }

    @Test
    public void marshallToJson_BooleanFieldView_MarshalledSuccessfully() throws Exception {
        FieldExport.BooleanFieldExport booleanFieldView = FieldExport.BooleanFieldExport.builder()
                .id(1123L)
                .name("BooleanField")
                .nullable(true)
                .build();

        String fieldAsString = objectMapper.writeValueAsString(booleanFieldView);

        assertThat(objectMapper.readValue(fieldAsString, FieldExport.class))
                .isEqualToComparingFieldByField(booleanFieldView);
    }

    @Test
    public void marshallToJson_LongFieldView_MarshalledSuccessfully() throws Exception {
        FieldExport.LongFieldExport longFieldView = FieldExport.LongFieldExport.builder()
                .id(421L)
                .name("longField")
                .nullable(false)
                .build();

        String fieldAsString = objectMapper.writeValueAsString(longFieldView);

        assertThat(objectMapper.readValue(fieldAsString, FieldExport.class))
                .isEqualToComparingFieldByField(longFieldView);
    }

    @Test
    public void marshallToJson_FloatFieldView_MarshalledSuccessfully() throws Exception {
        FieldExport.FloatFieldExport floatFieldView = FieldExport.FloatFieldExport.builder()
                .id(421L)
                .name("floatField")
                .nullable(false)
                .build();

        String fieldAsString = objectMapper.writeValueAsString(floatFieldView);

        assertThat(objectMapper.readValue(fieldAsString, FieldExport.class))
                .isEqualToComparingFieldByField(floatFieldView);
    }

    @Test
    public void marshallToJson_IntegerFieldView_MarshalledSuccessfully() throws Exception {
        FieldExport.IntegerFieldExport integerFieldView = FieldExport.IntegerFieldExport.builder()
                .id(421L)
                .name("IntegerField")
                .nullable(false)
                .build();

        String fieldAsString = objectMapper.writeValueAsString(integerFieldView);

        assertThat(objectMapper.readValue(fieldAsString, FieldExport.class))
                .isEqualToComparingFieldByField(integerFieldView);
    }

    @Test
    public void marshallToJson_DateFieldView_MarshalledSuccessfully() throws Exception {
        FieldExport.DateFieldExport dateFieldView = FieldExport.DateFieldExport.builder()
                .id(421L)
                .name("DateField")
                .nullable(false)
                .format("dd/mm/yyyy")
                .build();

        String fieldAsString = objectMapper.writeValueAsString(dateFieldView);

        assertThat(objectMapper.readValue(fieldAsString, FieldExport.class))
                .isEqualToComparingFieldByField(dateFieldView);
    }

    @Test
    public void marshallToJson_TimestampFieldView_MarshalledSuccessfully() throws Exception {
        FieldExport.TimestampFieldExport timestampFieldView = FieldExport.TimestampFieldExport.builder()
                .id(421L)
                .name("TimestampField")
                .nullable(false)
                .format("dd/mm/yyyy")
                .build();

        String fieldAsString = objectMapper.writeValueAsString(timestampFieldView);

        assertThat(objectMapper.readValue(fieldAsString, FieldExport.class))
                .isEqualToComparingFieldByField(timestampFieldView);
    }
}