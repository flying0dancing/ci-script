package com.lombardrisk.ignis.server.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@UtilityClass
public class S3Utils {

    public static Stream<S3ObjectSummary> streamBucketContent(
            final AmazonS3 s3Client, final String bucketName, final String prefix) {
        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withPrefix(prefix)
                .withMaxKeys(S3ObjectSummarySpliterator.DEFAULT_BUCKETING);

        S3ObjectSummarySpliterator spliterator = new S3ObjectSummarySpliterator(s3Client, request);
        return StreamSupport.stream(spliterator, false);
    }

    private static final class S3ObjectSummarySpliterator implements Spliterator<S3ObjectSummary> {

        public static final int DEFAULT_BUCKETING = 100;
        private final AmazonS3 s3Client;
        private final ListObjectsV2Request request;

        private S3ObjectSummarySpliterator(final AmazonS3 s3Client, final ListObjectsV2Request request) {
            this.s3Client = s3Client;
            this.request = request;
        }

        /**
         * Indicates whether there is more in the S3 collection to iterate over.
         *
         * @param action The action passed in by the stream, this needs to be applied to each item in the S3 collection
         * @return true if there are <b>more</b> objects in the s3 bucket, false if there are none
         */
        @Override
        public boolean tryAdvance(final Consumer<? super S3ObjectSummary> action) {
            ListObjectsV2Result result = s3Client.listObjectsV2(request);
            List<S3ObjectSummary> objectSummaries = result.getObjectSummaries();
            objectSummaries.forEach(action);

            String token = result.getNextContinuationToken();
            request.setContinuationToken(token);

            return result.isTruncated();
        }

        @Override
        public Spliterator<S3ObjectSummary> trySplit() {
            //splitting functionality not created yet
            return null;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return DISTINCT;
        }
    }
}