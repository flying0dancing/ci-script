package com.lombardrisk.ignis.spark.api.staging;

import com.lombardrisk.ignis.spark.api.fixture.Populated;
import org.junit.Test;

import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static org.assertj.core.api.Assertions.assertThat;

public class DatasetPropertiesTest {

    @Test
    public void marshalling() throws Exception {
        DatasetProperties datasetProperties = Populated.datasetProperties().build();

        String valueAsString = MAPPER.writeValueAsString(datasetProperties);

        assertThat(MAPPER.readValue(valueAsString, DatasetProperties.class))
                .isEqualTo(datasetProperties);
    }
}