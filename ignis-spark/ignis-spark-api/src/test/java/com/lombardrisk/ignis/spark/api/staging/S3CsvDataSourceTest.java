package com.lombardrisk.ignis.spark.api.staging;

import com.lombardrisk.ignis.spark.api.staging.datasource.DataSource;
import com.lombardrisk.ignis.spark.api.staging.datasource.S3CsvDataSource;
import org.junit.Test;

import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static org.assertj.core.api.Assertions.assertThat;

public class S3CsvDataSourceTest {

    @Test
    public void marshalToJson() throws Exception {
        S3CsvDataSource dataSource = S3CsvDataSource.builder()
                .header(true)
                .s3Protocol("s3a")
                .s3Key("s3Key")
                .s3Bucket("s3Bucket")
                .build();

        String dataSourceAsString = MAPPER.writeValueAsString(dataSource);

        assertThat(MAPPER.readValue(dataSourceAsString, DataSource.class))
                .isEqualTo(dataSource);
    }

    @Test
    public void getFileStreamPath_ReturnsFilePath() {
        S3CsvDataSource dataSource = S3CsvDataSource.builder()
                .header(true)
                .s3Protocol("s3a")
                .s3Key("s3Key")
                .s3Bucket("s3Bucket")
                .build();

        String filePath = dataSource.fileStreamPath();

        assertThat(filePath)
                .isEqualTo("s3a://s3Bucket/s3Key");
    }
}