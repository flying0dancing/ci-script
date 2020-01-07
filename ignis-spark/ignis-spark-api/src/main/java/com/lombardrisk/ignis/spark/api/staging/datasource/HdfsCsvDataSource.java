package com.lombardrisk.ignis.spark.api.staging.datasource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HdfsCsvDataSource implements DataSource {

    private static final long serialVersionUID = -6467391738110740444L;

    private String localPath;
    private String hdfsPath;
    private boolean header;

    private int start = 0 ;
    private int end = 0;

    @Override
    @JsonIgnore
    public String fileStreamPath() {
        return hdfsPath;
    }

}