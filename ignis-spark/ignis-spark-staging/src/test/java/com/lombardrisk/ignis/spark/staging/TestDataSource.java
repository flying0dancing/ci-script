package com.lombardrisk.ignis.spark.staging;

import com.lombardrisk.ignis.spark.api.staging.datasource.DataSource;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class TestDataSource implements DataSource {

    private static final long serialVersionUID = 4206372727929384632L;

    private final String filePath;
    @Getter
    private final boolean header;

    @Override
    public String fileStreamPath() {
        return filePath;
    }
}
