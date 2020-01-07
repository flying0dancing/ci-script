package com.lombardrisk.ignis.hadoop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class HadoopFSMetrics {
    private static final BigDecimal ONE_MB = new BigDecimal(FileUtils.ONE_MB);

    @JsonProperty("CapacityTotal")
    private Long capacityTotal;

    @JsonProperty("CapacityUsed")
    private Long capacityUsed;

    @JsonProperty("CapacityRemaining")
    private Long capacityRemaining;

    public Long getCapacityTotalMB() {
        return convertToMB(capacityTotal);
    }

    public Long getCapacityUsedMB() {
        return convertToMB(capacityUsed);
    }

    public Long getCapacityRemainingMB() {
        return convertToMB(capacityRemaining);
    }

    private static Long convertToMB(final Long bytes) {
        return new BigDecimal(bytes)
                .divide(ONE_MB, RoundingMode.HALF_UP)
                .longValue();
    }
}
