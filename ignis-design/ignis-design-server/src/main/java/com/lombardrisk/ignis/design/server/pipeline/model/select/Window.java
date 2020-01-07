package com.lombardrisk.ignis.design.server.pipeline.model.select;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OrderBy;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Collections.emptySet;

@Data
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Window {

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "PIPELINE_STEP_WINDOW_PARTITION",
            joinColumns = { @JoinColumn(name = "PIPELINE_STEP_SELECT_ID", referencedColumnName = "ID") })
    @Column(name = "FIELD_NAME")
    private Set<String> partitions = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "PIPELINE_STEP_WINDOW_ORDER",
            joinColumns = { @JoinColumn(name = "PIPELINE_STEP_SELECT_ID", referencedColumnName = "ID") })
    @OrderBy("PRIORITY")
    private Set<Order> orders = new LinkedHashSet<>();

    public static Window none() {
        return new Window(emptySet(), emptySet());
    }
}
