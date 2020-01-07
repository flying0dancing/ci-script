package com.lombardrisk.ignis.spark.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class DatasetTableLookup {

    @NonNull
    private String datasetName;
    private String predicate;
    private Long rowKeySeed;
    private Long datasetId;
    private Long recordsCount;

    public DatasetTableLookupBuilder copy() {
        return DatasetTableLookup.builder()
                .datasetName(this.datasetName)
                .predicate(this.predicate)
                .rowKeySeed(this.rowKeySeed)
                .datasetId(this.datasetId)
                .recordsCount(this.recordsCount);
    }
}
