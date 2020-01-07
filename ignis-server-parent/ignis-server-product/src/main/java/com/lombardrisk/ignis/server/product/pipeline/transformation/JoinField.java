package com.lombardrisk.ignis.server.product.pipeline.transformation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lombardrisk.ignis.server.product.table.model.Field;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PIPELINE_JOIN_FIELD")
public class JoinField {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "LEFT_JOIN_FIELD_ID")
    private Long leftJoinFieldId;

    @Column(name = "RIGHT_JOIN_FIELD_ID")
    private Long rightJoinFieldId;

    @ManyToOne
    @JoinColumn(name = "LEFT_JOIN_FIELD_ID", insertable = false, updatable = false)
    @JsonIgnore
    private Field leftJoinField;

    @ManyToOne
    @JoinColumn(name = "RIGHT_JOIN_FIELD_ID", insertable = false, updatable = false)
    @JsonIgnore
    private Field rightJoinField;
}
