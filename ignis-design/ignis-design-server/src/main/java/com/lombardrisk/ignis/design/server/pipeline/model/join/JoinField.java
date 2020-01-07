package com.lombardrisk.ignis.design.server.pipeline.model.join;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
}
