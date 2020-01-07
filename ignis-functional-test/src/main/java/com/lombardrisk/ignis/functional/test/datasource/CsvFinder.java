package com.lombardrisk.ignis.functional.test.datasource;

public interface CsvFinder {

    CsvFinder LOCAL_CSV_FINDER = localPath -> localPath;

    String findPath(final String localPath);
}
