package com.lombardrisk.ignis.spark.core.phoenix.ranged;

import com.google.common.base.Preconditions;
import io.vavr.Tuple2;
import lombok.NonNull;

import static com.lombardrisk.ignis.pipeline.step.api.DatasetFieldName.ROW_KEY;

public final class PhoenixTableRowKey {

    private static final int BIT_SHIFT_VALUE = 32;
    private static final Long UNSIGNED_MAX_INT = 0xFFFFFFFFL;

    private final Integer seed;

    private PhoenixTableRowKey(final Integer seed) {
        this.seed = seed;
    }

    private static boolean lessThanMaxUnsignedInt(final Long longValue) {
        return Long.compareUnsigned(longValue, UNSIGNED_MAX_INT) < 0;
    }

    /**
     * Currently assume that unsigned int is big enough for seed and row sequence.
     */
    public static PhoenixTableRowKey of(@NonNull final Long seed) {
        Preconditions.checkArgument(lessThanMaxUnsignedInt(seed), "Run key exceeds range of unsigned integer");
        return new PhoenixTableRowKey(seed.intValue());
    }

    /**
     * HBase primary key, used to avoid "Hot spotting"
     * When you write data to HBase (NoSql database)
     * When you increase sequence number. read the article
     * https://sematext.com/blog/hbasewd-avoid-regionserver-hotspotting-despite-writing-records-with-sequential-keys/
     */
    public Long buildRowKey(@NonNull final Long rowSeq) {
        Preconditions.checkArgument(
                lessThanMaxUnsignedInt(rowSeq),
                "Row sequence of dataset exceeds range of unsigned integer");
        return rowKeyHead() | rowSeq;
    }

    public Tuple2<Long, Long> getRowKeyRange() {
        Long start = rowKeyHead();
        Long end = start | UNSIGNED_MAX_INT;
        return new Tuple2<>(start, end);
    }

    private Long rowKeyHead() {
        return seed.longValue() << BIT_SHIFT_VALUE;
    }

    public String toPredicate() {
        Tuple2<Long, Long> range = getRowKeyRange();
        return String.format("%s >= %d and %s <= %d",
                ROW_KEY.getName(), range._1,
                ROW_KEY.getName(), range._2);
    }
}
