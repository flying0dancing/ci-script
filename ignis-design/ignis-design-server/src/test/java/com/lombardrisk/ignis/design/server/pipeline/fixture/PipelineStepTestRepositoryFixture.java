package com.lombardrisk.ignis.design.server.pipeline.fixture;

import com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestView;
import com.lombardrisk.ignis.data.common.fixtures.InMemoryRepository;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineStepRepository;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineStepTestRepository;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.design.server.pipeline.test.model.ActualDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.ExpectedDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.InputDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTest;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTestRow;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public class PipelineStepTestRepositoryFixture extends InMemoryRepository<PipelineStepTest>
        implements PipelineStepTestRepository {

    private final PipelineStepRepository pipelineStepRepository;
    private final PipelineStepTestRowRepositoryFixture pipelineStepTestRowRepository;

    @Override
    public PipelineStepTest save(final PipelineStepTest pipelineStepTest) {
        return addRelatedEntities(super.save(pipelineStepTest));
    }

    @Override
    public List<PipelineStepTest> findAllByPipelineStepId(final Long pipelineStepId) {
        return super.findAll().stream()
                .filter(test -> test.getPipelineStepId().equals(pipelineStepId))
                .map(this::addRelatedEntities)
                .collect(Collectors.toList());
    }

    @Override
    public List<StepTestView> findViewsByPipelineStepId(final Long pipelineStepId) {
        return findAllByPipelineStepId(pipelineStepId).stream()
                .map(this::toTestView)
                .collect(Collectors.toList());
    }

    @Override
    public List<PipelineStepTest> findAllByPipelineId(final long pipelineId) {
        List<Long> stepsForPipeline = pipelineStepRepository.findAll().stream()
                .filter(pipelineStep -> pipelineStep.getPipelineId().equals(pipelineId))
                .map(PipelineStep::getId)
                .collect(Collectors.toList());

        return super.findAll().stream()
                .filter(test -> stepsForPipeline.contains(test.getPipelineStepId()))
                .map(this::addRelatedEntities)
                .collect(Collectors.toList());
    }

    @Override
    public List<StepTestView> findViewsByPipelineId(final long pipelineId) {
        return findAllByPipelineId(pipelineId).stream()
                .map(this::toTestView)
                .collect(Collectors.toList());
    }

    @Override
    public Option<PipelineStepTest> findById(long id) {
        return Option.ofOptional(super.findById(id))
                .map(this::addRelatedEntities);
    }

    @Override
    public Option<StepTestView> findViewById(final long id) {
        return findById(id).map(this::toTestView);
    }

    @Override
    public void saveAll(final List<PipelineStepTest> stepTests) {
        stepTests.forEach(this::save);
    }

    private PipelineStepTest addRelatedEntities(final PipelineStepTest pipelineStepTest) {
        PipelineStep pipelineStep = pipelineStepRepository.findById(pipelineStepTest.getPipelineStepId()).get();
        pipelineStepTest.setPipelineStep(pipelineStep);

        Set<InputDataRow> inputDataRows = pipelineStepTestRowRepository.findAll().stream()
                .filter(row -> row.getPipelineStepTestId().equals(pipelineStepTest.getId()))
                .filter(row -> row.getType().equals(PipelineStepTestRow.Type.INPUT))
                .map(InputDataRow.class::cast)
                .collect(Collectors.toSet());
        Set<ExpectedDataRow> expectedDataRows = pipelineStepTestRowRepository.findAll().stream()
                .filter(row -> row.getPipelineStepTestId().equals(pipelineStepTest.getId()))
                .filter(row -> row.getType().equals(PipelineStepTestRow.Type.EXPECTED))
                .map(ExpectedDataRow.class::cast)
                .collect(Collectors.toSet());

        Set<ActualDataRow> actualDataRows = pipelineStepTestRowRepository.findAll().stream()
                .filter(row -> row.getPipelineStepTestId().equals(pipelineStepTest.getId()))
                .filter(row -> row.getType().equals(PipelineStepTestRow.Type.ACTUAL))
                .map(ActualDataRow.class::cast)
                .collect(Collectors.toSet());

        pipelineStepTest.setInputData(inputDataRows);
        pipelineStepTest.setExpectedData(expectedDataRows);
        pipelineStepTest.setActualData(actualDataRows);
        return pipelineStepTest;
    }

    private StepTestView toTestView(final PipelineStepTest test) {
        return StepTestView.builder()
                .id(test.getId())
                .name(test.getName())
                .description(test.getDescription())
                .pipelineId(test.getPipelineStep().getPipelineId())
                .pipelineStepId(test.getPipelineStepId())
                .testReferenceDate(test.getTestReferenceDate())
                .status(test.getPipelineStepStatus())
                .build();
    }
}
