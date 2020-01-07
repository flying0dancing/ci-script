package com.lombardrisk.ignis.server.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static com.lombardrisk.ignis.server.util.S3UtilTest.createSummary;
import static com.lombardrisk.ignis.test.config.AdminUser.BASIC_AUTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(Enclosed.class)
public class DatasetSourceFileControllerIT {

    @RunWith(SpringRunner.class)
    @IntegrationTestConfig
    @TestPropertySource(
            properties = {
                    "IGNIS_HOST=localhost",
                    "ignis.host=localhost",
                    "env.hostname=localhost",
                    "ignis.home=.",
                    "dataset.source.location.localPath=src/test/resources/sourcefiles",
                    "dataset.source.location.remotePath=src/test/resources/sourcefiles",
                    "dataset.source.location.type=LOCAL",
            }
    )
    public static class Local {

        @Autowired
        private MockMvc mvc;

        @Test
        public void testGetAllFileNames() throws Exception {
            mvc.perform(get("/api/internal/datasets/source/files")
                    .with(BASIC_AUTH))

                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0]", is("employee.json")));
        }
    }

    @RunWith(SpringRunner.class)
    @IntegrationTestConfig
    @TestPropertySource(
            properties = {
                    "IGNIS_HOST=localhost",
                    "ignis.host=localhost",
                    "env.hostname=localhost",
                    "ignis.home=.",
                    "dataset.source.location.s3.bucket=TEST_BUCKET",
                    "dataset.source.location.s3.prefix=fcr-csvs",
                    "dataset.error.location.s3.bucket=TEST_BUCKET",
                    "dataset.error.location.s3.prefix=fcr-csvs",
                    "dataset.source.location.s3.credentialsSource=ENVIRONMENT",
                    "dataset.source.location.s3.region=eu-west-1",
                    "dataset.source.location.type=S3",
                    "s3.protocol=s3a"
            }
    )
    public static class S3 {

        @Autowired
        private MockMvc mvc;

        @MockBean
        private AmazonS3 amazonS3;

        @Mock
        private ListObjectsV2Result listObjectsV2Result;

        @Captor
        private ArgumentCaptor<ListObjectsV2Request> requestCaptor;

        @Before
        public void setUp() {
            when(amazonS3.listObjectsV2(any(ListObjectsV2Request.class)))
                    .thenReturn(listObjectsV2Result);
        }

        @Test
        public void testGetAllFileNames() throws Exception {
            when(listObjectsV2Result.getObjectSummaries())
                    .thenReturn(Arrays.asList(
                            createSummary("file_1.csv"),
                            createSummary("file_2.csv"),
                            createSummary("file_3.csv"),
                            createSummary("file_4.csv")
                    ));

            mvc.perform(get("/api/internal/datasets/source/files")
                    .with(BASIC_AUTH))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0]", is("file_1.csv")))
                    .andExpect(jsonPath("$[1]", is("file_2.csv")))
                    .andExpect(jsonPath("$[2]", is("file_3.csv")))
                    .andExpect(jsonPath("$[3]", is("file_4.csv")));
        }

        @Test
        public void getAllFileNames_QueriesS3Bucket() throws Exception {
            mvc.perform(get("/api/internal/datasets/source/files")
                    .with(BASIC_AUTH))
                    .andExpect(status().isOk());

            verify(amazonS3).listObjectsV2(requestCaptor.capture());
            assertThat(requestCaptor.getValue().getBucketName())
                    .isEqualTo("TEST_BUCKET");
            assertThat(requestCaptor.getValue().getPrefix())
                    .isEqualTo("fcr-csvs");
        }
    }
}