package com.lombardrisk.ignis.functional.test.datasource;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class S3CsvFinder implements CsvFinder {

    private final String s3Prefix;

    @Override
    public String findPath(final String localPath) {
        return s3Prefix + "/" + LOCAL_CSV_FINDER.findPath(localPath);
    }
}
