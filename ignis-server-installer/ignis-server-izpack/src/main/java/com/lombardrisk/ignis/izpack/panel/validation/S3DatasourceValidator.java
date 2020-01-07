package com.lombardrisk.ignis.izpack.panel.validation;

import com.izforge.izpack.api.data.InstallData;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.lombardrisk.ignis.izpack.core.InstallerProps.DATASET_ERROR_S3_BUCKET_PROP;
import static com.lombardrisk.ignis.izpack.core.InstallerProps.DATASET_ERROR_S3_PREFIX_PROP;
import static com.lombardrisk.ignis.izpack.core.InstallerProps.DATASET_SOURCE_S3_BUCKET_PROP;
import static com.lombardrisk.ignis.izpack.core.InstallerProps.DATASET_SOURCE_S3_PREFIX_PROP;
import static com.lombardrisk.ignis.izpack.core.InstallerProps.S3_PROTOCOL_PROP;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class S3DatasourceValidator extends SimpleDataValidator {

    private static final int MIN_BUCKET_LENGTH = 3;
    private static final int MAX_BUCKET_LENGTH = 63;

    @Override
    public Stream<Optional<String>> validate(final InstallData installData) {
        return Stream.of(
                validateS3Bucket(installData, DATASET_SOURCE_S3_BUCKET_PROP),
                validateS3Prefix(installData, DATASET_SOURCE_S3_PREFIX_PROP),
                validateS3Bucket(installData, DATASET_ERROR_S3_BUCKET_PROP),
                validateS3Prefix(installData, DATASET_ERROR_S3_PREFIX_PROP),
                validateS3Protocol(installData, S3_PROTOCOL_PROP));
    }

    private static Optional<String> validateS3Bucket(
            final InstallData installData,
            final String datasetSourceS3BucketProp) {

        String s3Bucket = installData.getVariable(datasetSourceS3BucketProp);

        if (isBlank(s3Bucket)) {
            return Optional.of("[" + datasetSourceS3BucketProp + "] S3 bucket name must not be blank");
        }
        if (!isValidLength(s3Bucket)) {
            return Optional.of("[" + datasetSourceS3BucketProp + "] S3 bucket name must be between 3 and 63 characters,"
                    + " not contain / or  _ or uppercase characters");
        }
        return Optional.empty();
    }

    private static boolean isValidLength(final String s3Bucket) {
        boolean doesNotContainUpperCase = Objects.equals(s3Bucket.toLowerCase(), s3Bucket);

        boolean isWithinBounds =
                s3Bucket.length() >= MIN_BUCKET_LENGTH
                        && s3Bucket.length() <= MAX_BUCKET_LENGTH;

        return isWithinBounds
                && doesNotContainUpperCase
                && !s3Bucket.contains("/");
    }

    private static Optional<String> validateS3Prefix(
            final InstallData installData,
            final String s3PrefixProp) {

        String s3Prefix = installData.getVariable(s3PrefixProp);

        if (s3Prefix.startsWith("/")
                || s3Prefix.endsWith("/")) {
            return Optional.of("[" + s3PrefixProp + "]"
                    + " S3 datasource root dir must not start or end with / character");
        }
        return Optional.empty();
    }

    private static Optional<String> validateS3Protocol(
            final InstallData installData,
            final String s3ProtocolProp) {
        String s3Protocol = installData.getVariable(s3ProtocolProp);

        if (!s3Protocol.equals("s3a") && !s3Protocol.equals("s3")) {
            return Optional.of("[" + s3ProtocolProp + "] must be either s3a or s3");
        }

        return Optional.empty();
    }
}
