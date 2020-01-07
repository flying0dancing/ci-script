package com.lombardrisk.ignis.izpack.panel.validation;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator.Status;
import com.lombardrisk.ignis.izpack.InstallDataFixtures;
import org.junit.Before;
import org.junit.Test;

import static com.lombardrisk.ignis.izpack.core.InstallerProps.DATASET_ERROR_S3_BUCKET_PROP;
import static com.lombardrisk.ignis.izpack.core.InstallerProps.DATASET_ERROR_S3_PREFIX_PROP;
import static com.lombardrisk.ignis.izpack.core.InstallerProps.DATASET_SOURCE_S3_BUCKET_PROP;
import static com.lombardrisk.ignis.izpack.core.InstallerProps.DATASET_SOURCE_S3_PREFIX_PROP;
import static com.lombardrisk.ignis.izpack.core.InstallerProps.S3_PROTOCOL_PROP;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;

public class S3DatasourceValidatorTest {

    private InstallData installData = InstallDataFixtures.installData();
    private S3DatasourceValidator s3DatasourceValidator = new S3DatasourceValidator();

    @Before
    public void setup() {
        installData.setVariable(DATASET_SOURCE_S3_BUCKET_PROP, "s3-bucket");
        installData.setVariable(DATASET_SOURCE_S3_PREFIX_PROP, "source-prefix");
        installData.setVariable(DATASET_ERROR_S3_BUCKET_PROP, "s3-bucket-errors");
        installData.setVariable(DATASET_ERROR_S3_PREFIX_PROP, "errors-prefix");
        installData.setVariable(S3_PROTOCOL_PROP, "s3a");
    }

    @Test
    public void validateData_ReturnsOK() {
        installData.setVariable(DATASET_SOURCE_S3_BUCKET_PROP, "ok-s3-bucket");
        installData.setVariable(DATASET_SOURCE_S3_PREFIX_PROP, "ok-source");
        installData.setVariable(DATASET_ERROR_S3_BUCKET_PROP, "ok-s3-bucket-errors");
        installData.setVariable(DATASET_ERROR_S3_PREFIX_PROP, "ok-errors");

        assertThat(s3DatasourceValidator.validateData(installData))
                .isEqualTo(Status.OK);
    }

    @Test
    public void validateData_S3ProtocolS3A_ReturnsOk() {
        installData.setVariable(S3_PROTOCOL_PROP, "s3a");

        assertThat(s3DatasourceValidator.validateData(installData))
                .isEqualTo(Status.OK);
    }

    @Test
    public void validateData_S3ProtocolS3_ReturnsOk() {
        installData.setVariable(S3_PROTOCOL_PROP, "s3");

        assertThat(s3DatasourceValidator.validateData(installData))
                .isEqualTo(Status.OK);
    }

    @Test
    public void validateData_BlankSourceBucketName_ReturnsError() {
        installData.setVariable(DATASET_SOURCE_S3_BUCKET_PROP, "");

        assertThat(s3DatasourceValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        installData.setVariable(DATASET_SOURCE_S3_BUCKET_PROP, null);

        assertThat(s3DatasourceValidator.validateData(installData))
                .isEqualTo(Status.ERROR);
        assertThat(s3DatasourceValidator.getErrorMessageId())
                .contains(DATASET_SOURCE_S3_BUCKET_PROP)
                .contains("must not be blank");
    }

    @Test
    public void validateData_InvalidSourceBucketNameLength_ReturnsError() {
        installData.setVariable(DATASET_SOURCE_S3_BUCKET_PROP, repeat("a", 64));

        assertThat(s3DatasourceValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        installData.setVariable(DATASET_SOURCE_S3_BUCKET_PROP, "aa");

        assertThat(s3DatasourceValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        assertThat(s3DatasourceValidator.getErrorMessageId())
                .contains(DATASET_SOURCE_S3_BUCKET_PROP)
                .contains("must be between 3 and 63 characters")
                .contains("not contain / or  _ or uppercase characters");
    }

    @Test
    public void validateData_S3ErrorsPrefixHasSlashes_ReturnsError() {
        installData.setVariable(DATASET_ERROR_S3_PREFIX_PROP, "/prefix");

        assertThat(s3DatasourceValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        installData.setVariable(DATASET_ERROR_S3_PREFIX_PROP, "/prefix/");

        assertThat(s3DatasourceValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        installData.setVariable(DATASET_ERROR_S3_PREFIX_PROP, "prefix/");

        assertThat(s3DatasourceValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        assertThat(s3DatasourceValidator.getErrorMessageId())
                .contains(DATASET_ERROR_S3_PREFIX_PROP)
                .contains("must not start or end with / character");
    }

    @Test
    public void validateData_BlankErrorsBucketName_ReturnsError() {
        installData.setVariable(DATASET_ERROR_S3_BUCKET_PROP, "");

        assertThat(s3DatasourceValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        installData.setVariable(DATASET_ERROR_S3_BUCKET_PROP, null);

        assertThat(s3DatasourceValidator.validateData(installData))
                .isEqualTo(Status.ERROR);
        assertThat(s3DatasourceValidator.getErrorMessageId())
                .contains(DATASET_ERROR_S3_BUCKET_PROP)
                .contains("must not be blank");
    }

    @Test
    public void validateData_InvalidErrorsBucketNameLength_ReturnsError() {
        installData.setVariable(DATASET_ERROR_S3_BUCKET_PROP, repeat("a", 64));

        assertThat(s3DatasourceValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        installData.setVariable(DATASET_ERROR_S3_BUCKET_PROP, "aa");

        assertThat(s3DatasourceValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        assertThat(s3DatasourceValidator.getErrorMessageId())
                .contains(DATASET_ERROR_S3_BUCKET_PROP)
                .contains("must be between 3 and 63 characters")
                .contains("not contain / or  _ or uppercase characters");
    }

    @Test
    public void validateData_S3SourcePrefixHasSlashes_ReturnsError() {
        installData.setVariable(DATASET_ERROR_S3_PREFIX_PROP, "/prefix");

        assertThat(s3DatasourceValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        installData.setVariable(DATASET_ERROR_S3_PREFIX_PROP, "/prefix/");

        assertThat(s3DatasourceValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        installData.setVariable(DATASET_ERROR_S3_PREFIX_PROP, "prefix/");

        assertThat(s3DatasourceValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        assertThat(s3DatasourceValidator.getErrorMessageId())
                .contains(DATASET_ERROR_S3_PREFIX_PROP)
                .contains("must not start or end with / character");
    }

    @Test
    public void validateData_S3ProtocolInvalid_ReturnsError() {
        installData.setVariable(S3_PROTOCOL_PROP, "invalid");

        assertThat(s3DatasourceValidator.validateData(installData))
                .isEqualTo(Status.ERROR);

        assertThat(s3DatasourceValidator.getErrorMessageId())
                .contains(S3_PROTOCOL_PROP)
                .contains("[s3.protocol] must be either s3a or s3");
    }
}