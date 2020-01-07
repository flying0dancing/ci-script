package com.lombardrisk.ignis.spark.staging.execution.error;

import com.lombardrisk.ignis.spark.api.staging.StagingErrorOutput;
import com.lombardrisk.ignis.spark.staging.exception.DatasetStoreException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Slf4j
@AllArgsConstructor
public class ErrorFileService implements Serializable {

    private static final long serialVersionUID = 3853494706935225637L;
    private final SparkSession sparkSession;

    /**
     * Temporary location is necessary as spark saves RDD in multiple parts, this applies for both S3 and HDFS storage.
     * The rdd is saved in multiple parts by saveAsTextFile and then merged using the FileUtil
     *
     * @param invalidRDD         Spark RDD containing validation failures
     * @param stagingErrorOutput DTO Containing URI to error FileSystem and paths in which to store error files
     */
    public void saveErrorFile(final JavaRDD<String> invalidRDD, final StagingErrorOutput stagingErrorOutput) {
        String tempPath = stagingErrorOutput.getTemporaryFilePath();
        String errorFilePath = stagingErrorOutput.getErrorFilePath();
        log.debug("Saving invalid rdd to temporary location {}", tempPath);

        invalidRDD.saveAsTextFile(tempPath);

        Configuration hadoopConfiguration = sparkSession.sparkContext().hadoopConfiguration();
        log.debug("Opening file system with uri {}", stagingErrorOutput.getErrorFileSystemUri());

        try (FileSystem fileSystem = FileSystem.get(
                new URI(stagingErrorOutput.getErrorFileSystemUri()), hadoopConfiguration)) {

            log.debug("Copying temp file {} to {}", new Path(tempPath), new Path(errorFilePath));

            FileUtil.copyMerge(
                    fileSystem, new Path(tempPath),
                    fileSystem, new Path(errorFilePath),
                    true, hadoopConfiguration, EMPTY);

        } catch (URISyntaxException | IOException e) {
            throw new DatasetStoreException("An error occurred while storing the invalid dataset", e);
        }
    }
}
