package com.lombardrisk.ignis.server.product.pipeline.transformation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lombardrisk.ignis.server.product.pipeline.details.SchemaDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Entity
@Table(name = "PIPELINE_STEP_JOIN")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Join {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "LEFT_SCHEMA_ID")
    private Long leftSchemaId;

    @Column(name = "RIGHT_SCHEMA_ID")
    private Long rightSchemaId;

    @Column(name = "JOIN_TYPE")
    @Enumerated(value = EnumType.STRING)
    private JoinType joinType;

    //retrieval only
    @ManyToOne
    @JoinColumn(name = "LEFT_SCHEMA_ID", insertable = false, updatable = false)
    @JsonIgnore
    private SchemaDetails leftSchema;

    @ManyToOne
    @JoinColumn(name = "RIGHT_SCHEMA_ID", insertable = false, updatable = false)
    @JsonIgnore
    private SchemaDetails rightSchema;

    @OrderBy("ID")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "PIPELINE_JOIN_ID", nullable = false)
    private Set<JoinField> joinFields = new LinkedHashSet<>();

    public Set<Long> leftFieldIds() {
        return joinFields.stream().map(JoinField::getLeftJoinFieldId)
                .collect(Collectors.toSet());
    }

    public Set<Long> rightFieldIds() {
        return joinFields.stream().map(JoinField::getRightJoinFieldId)
                .collect(Collectors.toSet());
    }

    public enum JoinType {
        INNER,
        LEFT,
        FULL_OUTER
    }
}
