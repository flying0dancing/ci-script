package com.lombardrisk.ignis.spark.api.staging.datasource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class S3CsvDataSource implements DataSource {

    private static final long serialVersionUID = 2444636502599954396L;

    private String s3Protocol;
    private String s3Bucket;
    private String s3Key;
    private boolean header;

    @Override
    @JsonIgnore
    public String fileStreamPath() {
        return s3Protocol + "://" + s3Bucket + "/" + s3Key;
    }

}
