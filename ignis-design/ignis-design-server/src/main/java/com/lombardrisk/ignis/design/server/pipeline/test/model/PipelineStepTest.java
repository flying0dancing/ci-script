package com.lombardrisk.ignis.design.server.pipeline.test.model;

import com.lombardrisk.ignis.client.design.pipeline.test.StepTestStatus;
import com.lombardrisk.ignis.common.stream.StreamUtils;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@Table(name = "PIPELINE_STEP_TEST")
public class PipelineStepTest implements Identifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "TEST_REFERENCE_DATE")
    private LocalDate testReferenceDate;

    @Column(name = "PIPELINE_STEP_ID")
    private Long pipelineStepId;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private StepTestStatus pipelineStepStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PIPELINE_STEP_ID", nullable = false, updatable = false, insertable = false)
    private PipelineStep pipelineStep;

    //Read only
    @OrderBy("id ASC")
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "STEP_TEST_ID", nullable = false, updatable = false, insertable = false)
    private Set<InputDataRow> inputData;

    @OrderBy("id ASC")
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "STEP_TEST_ID", nullable = false, updatable = false, insertable = false)
    private Set<ExpectedDataRow> expectedData;

    @OrderBy("id ASC")
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "STEP_TEST_ID", nullable = false, updatable = false, insertable = false)
    private Set<ActualDataRow> actualData;

    public Stream<PipelineStepTestRow> dependantRows() {
        return StreamUtils.concat(
                getInputData().stream(),
                getExpectedData().stream(),
                getActualData().stream());
    }
}
