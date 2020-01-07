package com.lombardrisk.ignis.pipeline.step.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSpec {

    private String column;
    private Direction direction;

    public static OrderSpec column(final String column, final Direction direction) {
        return new OrderSpec(column, direction);
    }

    public enum Direction {
        ASC,
        DESC
    }
}
