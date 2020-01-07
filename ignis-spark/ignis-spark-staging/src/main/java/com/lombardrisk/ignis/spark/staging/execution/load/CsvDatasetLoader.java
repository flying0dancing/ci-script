package com.lombardrisk.ignis.spark.staging.execution.load;

import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import com.lombardrisk.ignis.spark.api.staging.datasource.DataSource;
import com.lombardrisk.ignis.spark.staging.datafields.DataRow;
import com.lombardrisk.ignis.spark.staging.datafields.IndexedDataField;
import com.lombardrisk.ignis.spark.staging.exception.DatasetLoaderException;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.springframework.stereotype.Component;
import scala.Tuple2;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import static com.lombardrisk.ignis.common.MapperUtils.mapListWithIndex;

@Slf4j
@Component
public class CsvDatasetLoader implements Serializable {

    private static final long serialVersionUID = 3698136231295334317L;

    private final transient JavaSparkContext javaSparkContext;

    public CsvDatasetLoader(final JavaSparkContext javaSparkContext) {
        this.javaSparkContext = javaSparkContext;
    }

    public JavaRDD<DataRow> load(final StagingDatasetConfig item) {
        return loadCsvSource(item.getSource())
                .map(CsvDatasetLoader::extractFields);
    }

    private JavaRDD<String> loadCsvSource(final DataSource src) {
        String filePath = src.fileStreamPath();
        log.debug("Load RDD from [{}]", filePath);

        JavaRDD<String> rdd = javaSparkContext.textFile(filePath, javaSparkContext.defaultParallelism());

        int start = src.isHeader() ? src.getStart() + 1 : src.getStart();
        int end = src.getEnd();

        if (start > 0 || end > 0) {
            log.debug("Exclude start and/or end rows from [{}]", filePath);

            return excludeStartEndRows(rdd, start, end == 0 ? Long.MAX_VALUE : end);
        }
        return rdd;
    }

    /**
     * Used to strip header off
     * Some files are like CSV but not quite, so there are some extra things we want to exclude
     * In some cases we want to get rid of the last few lines
     */
    private static JavaRDD<String> excludeStartEndRows(
            final JavaRDD<String> rdd,
            final long startIndex,
            final long endIndex) {
        Function<Tuple2<String, Long>, Boolean> checkIndex =
                t -> (t._2() >= startIndex) && (t._2() <= endIndex);

        return rdd.zipWithIndex()
                .filter(checkIndex)
                .map(Tuple2::_1);
    }

    /**
     * This returns a data row of indexed fields.
     * With the index being the index of the column in the CSV
     */
    private static DataRow extractFields(final String row) {
        try {
            List<String> rows = new CsvLineParser(row).parse(true);
            List<IndexedDataField> dataFields = mapListWithIndex(rows, IndexedDataField::new);

            log.trace("Mapped row\n   [{}]\nto {}", row, dataFields);
            return new DataRow(dataFields);

        } catch (final IOException e) {
            throw new DatasetLoaderException(e);
        }
    }
}