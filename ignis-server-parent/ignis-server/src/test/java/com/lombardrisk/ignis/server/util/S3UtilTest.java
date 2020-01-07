package com.lombardrisk.ignis.server.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class S3UtilTest {

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private ListObjectsV2Result result;

    @Captor
    private ArgumentCaptor<ListObjectsV2Request> requestArgumentCaptor;

    @Before
    public void setUp() {
        when(amazonS3.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenReturn(result);

        when(result.isTruncated())
                .thenReturn(false);

        when(result.getObjectSummaries())
                .thenReturn(Arrays.asList(
                        createSummary("1"),
                        createSummary("2")));
    }

    @Test
    public void stream_resultIsTruncated_ContinuesWithStream() {
        AtomicInteger numberOfTimesCalled = new AtomicInteger();
        when(result.isTruncated())
                .thenAnswer(invocation -> {
                    int timesCalled = numberOfTimesCalled.getAndIncrement();
                    return timesCalled != 2;
                });

        when(result.getObjectSummaries())
                .thenAnswer(invocation -> {
                    int timesCalled = numberOfTimesCalled.get();
                    if (timesCalled == 0) {
                        return Arrays.asList(
                                createSummary("1"),
                                createSummary("2"));
                    }

                    if (timesCalled == 1) {
                        return Arrays.asList(
                                createSummary("3"),
                                createSummary("4"));
                    }

                    return Arrays.asList(
                            createSummary("5"),
                            createSummary("6"));
                });

        List<String> keys = S3Utils.streamBucketContent(amazonS3, "testbucket", "pre")
                .map(S3ObjectSummary::getKey)
                .collect(Collectors.toList());

        assertThat(keys)
                .contains("1", "2", "3", "4", "5", "6");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void stream_resultIsTruncated_SetsContinuationToken() {

        AtomicInteger numberOfTimesCalled = new AtomicInteger(0);
        when(result.isTruncated())
                .thenAnswer(invocation -> {
                    int timesCalled = numberOfTimesCalled.getAndIncrement();
                    return timesCalled == 0;
                });

        when(result.getNextContinuationToken())
                .thenReturn("Continuation");

        S3Utils.streamBucketContent(amazonS3, "testBucket", "pre")
                .collect(Collectors.toList());

        verify(amazonS3, times(2)).listObjectsV2(requestArgumentCaptor.capture());

        List<ListObjectsV2Request> requests = requestArgumentCaptor.getAllValues();
        ListObjectsV2Request intialRequest = requests.get(0);
        ListObjectsV2Request secondRequest = requests.get(1);

        assertThat(intialRequest)
                .isSameAs(secondRequest);

        assertThat(secondRequest.getContinuationToken())
                .isEqualTo("Continuation");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void stream_createsCorrectRequest() {

        S3Utils.streamBucketContent(amazonS3, "testBucket", "pre")
                .collect(Collectors.toList());

        verify(amazonS3).listObjectsV2(requestArgumentCaptor.capture());

        ListObjectsV2Request request = requestArgumentCaptor.getValue();

        assertThat(request.getBucketName())
                .isEqualTo("testBucket");
        assertThat(request.getPrefix())
                .isEqualTo("pre");
    }

    public static S3ObjectSummary createSummary(final String key) {
        S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();

        ReflectionTestUtils.setField(s3ObjectSummary, "key", key);

        return s3ObjectSummary;
    }
}