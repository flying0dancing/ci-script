package com.lombardrisk.ignis.functional.test.config.properties;

import com.lombardrisk.ignis.functional.test.datasource.CsvFinder;
import com.lombardrisk.ignis.functional.test.datasource.S3CsvFinder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "csv")
@Getter
@Setter
public class CsvDataSourceProperties {

    private Source source;
    private String s3Prefix;

    public enum Source {
        S3,
        LOCAL
    }

    public CsvFinder csvFinder() {
        if (Source.S3 == source) {
            return new S3CsvFinder(s3Prefix);
        }

        return CsvFinder.LOCAL_CSV_FINDER;
    }
}
