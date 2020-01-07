package com.lombardrisk.ignis.spark.core.phoenix;

import com.lombardrisk.ignis.spark.core.phoenix.ranged.PhoenixTableRowKey;
import io.vavr.Tuple2;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PhoenixTableRowKeyTest {

    @Test
    public void getRowKeyRange_SeedZero_ReturnsRange() {
        PhoenixTableRowKey phoenixTableRowKey = PhoenixTableRowKey.of(0L);

        Tuple2<Long, Long> range = phoenixTableRowKey.getRowKeyRange();
        assertThat(range._1)
                .isEqualTo(0L);
        assertThat(range._2)
                .isEqualTo(4294967295L); //rowKey range is fixed at 4294967295L
    }

    @Test
    public void getRowKeyRange_SeedThree_ReturnsRangeShiftedByThree() {
        PhoenixTableRowKey phoenixTableRowKey = PhoenixTableRowKey.of(3L);

        Tuple2<Long, Long> range = phoenixTableRowKey.getRowKeyRange();
        assertThat(range._1)
                .isEqualTo(12884901888L);
        assertThat(range._2) //rowKey range is fixed at 4294967295L but shifted by 3 * 4294967295L
                .isEqualTo(12884901888L + 4294967295L);
    }

    @Test
    public void toPredicate_SeedThree_ReturnsRangeShiftedByThree() {
        PhoenixTableRowKey phoenixTableRowKey = PhoenixTableRowKey.of(3L);

        String range = phoenixTableRowKey.toPredicate();
        assertThat(range)
                .isEqualTo("ROW_KEY >= 12884901888 and ROW_KEY <= 17179869183");
    }

    @Test
    public void buildRowKey_SeedZeroFirstInSequence_ReturnsBitShiftedSeed() {
        PhoenixTableRowKey phoenixTableRowKey = PhoenixTableRowKey.of(0L);

        Long rowKey = phoenixTableRowKey.buildRowKey(1L);
        assertThat(rowKey)
                .isEqualTo(0x0000000000000001L)
                .isEqualTo(1L);
    }

    @Test
    public void buildRowKey_SeedZeroSecondInSequence_ReturnsBitShiftedSeedPlusOne() {
        PhoenixTableRowKey phoenixTableRowKey = PhoenixTableRowKey.of(0L);

        Long rowKey = phoenixTableRowKey.buildRowKey(2L);
        assertThat(rowKey)
                .isEqualTo(0x0000000000000002L)
                .isEqualTo(2L);
    }

    @Test
    public void buildRowKey_SeedOneFirstInSequence_ReturnsBitShiftedSeed() {
        PhoenixTableRowKey phoenixTableRowKey = PhoenixTableRowKey.of(1L);

        Long rowKey = phoenixTableRowKey.buildRowKey(1L);
        assertThat(rowKey)
                .isEqualTo(0x0000000100000001L)
                .isEqualTo(4294967297L);
    }

    @Test
    public void buildRowKey_SeedOneSecondInSequence_ReturnsBitShiftedSeedPlusOne() {
        long sequence = 2;
        PhoenixTableRowKey phoenixTableRowKey = PhoenixTableRowKey.of(1L);

        Long rowKey = phoenixTableRowKey.buildRowKey(sequence);
        assertThat(rowKey)
                .isEqualTo(0x0000000100000002L)
                .isEqualTo(4294967298L);
    }

    @Test
    public void buildRowKey_SeedOneThirdInSequence_ReturnsBitShiftedSeedPlusTwo() {
        long sequence = 3;
        PhoenixTableRowKey phoenixTableRowKey = PhoenixTableRowKey.of(1L);

        Long rowKey = phoenixTableRowKey.buildRowKey(sequence);
        assertThat(rowKey)
                .isEqualTo(0x0000000100000003L)
                .isEqualTo(4294967299L);
    }

    @Test
    public void buildRowKey_SequenceNumberExceedMaxRange_ThrowsException() {

        long maximumSizeOfRowKeyRange = 4294967297L;
        PhoenixTableRowKey phoenixTableRowKey = PhoenixTableRowKey.of(1301210L);

        assertThatThrownBy(() -> phoenixTableRowKey.buildRowKey(maximumSizeOfRowKeyRange))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Row sequence of dataset exceeds range of unsigned integer");
    }

    @SuppressWarnings("NumericOverflow")
    @Test
    public void constructor_RunKeyLargerThanUnsignedInt_ThrowsIllegalArgumentException() {
        long maxValue = 4294967295L;

        assertThatThrownBy(() -> PhoenixTableRowKey.of(maxValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Run key exceeds range of unsigned integer");
    }
}