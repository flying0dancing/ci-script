package com.lombardrisk.ignis.design.server.pipeline.test.model;

import com.lombardrisk.ignis.data.common.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DiscriminatorOptions;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "STEP_TEST_ROW")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorOptions(force = true)
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.STRING)
public abstract class PipelineStepTestRow implements Identifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "STEP_TEST_ID")
    private Long pipelineStepTestId;

    @Column(name = "SCHEMA_ID")
    private Long schemaId;

    @Column(name = "IS_RUN")
    private boolean run;

    @Column(name = "TYPE", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    public enum Type {
        INPUT, EXPECTED, ACTUAL
    }

    @OrderBy("fieldId ASC")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "STEP_TEST_ROW_ID", nullable = false)
    private Set<PipelineStepTestCell> cells = new LinkedHashSet<>();
}
