package com.lombardrisk.ignis.spark.api.staging;

import com.lombardrisk.ignis.spark.api.fixture.Populated;
import com.lombardrisk.ignis.spark.api.staging.datasource.DataSource;
import com.lombardrisk.ignis.spark.api.staging.datasource.HdfsCsvDataSource;
import org.junit.Test;

import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static org.assertj.core.api.Assertions.assertThat;

public class HdfsCsvDataSourceTest {

    @Test
    public void marshalToJson() throws Exception {
        HdfsCsvDataSource dataSource = Populated.csvDataSource()
                .header(true)
                .localPath("local")
                .hdfsPath("remote/0/physical_table")
                .build();

        String dataSourceAsString = MAPPER.writeValueAsString(dataSource);

        assertThat(MAPPER.readValue(dataSourceAsString, DataSource.class))
                .isEqualTo(dataSource);
    }

    @Test
    public void getFilePath_ReturnsFilePath() {
        HdfsCsvDataSource dataSource = Populated.csvDataSource()
                .header(true)
                .hdfsPath("hdfs://remote/0/physical_table")
                .build();

        String filePath = dataSource.fileStreamPath();

        assertThat(filePath)
                .isEqualTo("hdfs://remote/0/physical_table");
    }
}