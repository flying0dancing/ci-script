package com.lombardrisk.ignis.server.dataset.pipeline.invocation.model;

import com.lombardrisk.ignis.data.common.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@Table(name = "PIPELINE_INVOCATION")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PipelineInvocation implements Identifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "CREATED_TIME")
    private LocalDateTime createdTime;

    @Column(name = "SERVICE_REQUEST_ID")
    private Long serviceRequestId;

    @Column(name = "PIPELINE_ID")
    private Long pipelineId;

    @Column(name = "ENTITY_CODE")
    private String entityCode;

    @Column(name = "REFERENCE_DATE")
    private LocalDate referenceDate;

    @Valid
    @OrderBy("id ASC")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "PIPELINE_INVOCATION_ID", nullable = false)
    private Set<PipelineStepInvocation> steps;
}
