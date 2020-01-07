package com.lombardrisk.ignis.design.server.pipeline.model.select;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order implements Comparable<Order> {

    @Column(name = "FIELD_NAME")
    private String fieldName;

    @Column(name = "DIRECTION")
    @Enumerated(EnumType.STRING)
    private Direction direction;

    @Column(name = "PRIORITY")
    private Integer priority;

    @Override
    public int compareTo(final Order other) {
        return this.priority.compareTo(other.priority);
    }

    public enum Direction {
        ASC,
        DESC;
    }
}
