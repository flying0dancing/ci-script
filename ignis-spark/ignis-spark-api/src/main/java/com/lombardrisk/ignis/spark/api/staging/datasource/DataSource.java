package com.lombardrisk.ignis.spark.api.staging.datasource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = HdfsCsvDataSource.class, name = "hdfs"),
        @JsonSubTypes.Type(value = S3CsvDataSource.class, name = "s3")
})
public interface DataSource extends Serializable {

    @JsonIgnore
    String fileStreamPath();

    boolean isHeader();

    default int getStart() {
        return 0;
    }

    default int getEnd() {
        return 0;
    }

}