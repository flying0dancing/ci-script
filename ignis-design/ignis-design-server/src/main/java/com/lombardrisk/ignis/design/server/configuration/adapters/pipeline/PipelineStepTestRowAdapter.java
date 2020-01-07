package com.lombardrisk.ignis.design.server.configuration.adapters.pipeline;

import com.lombardrisk.ignis.design.server.jpa.pipeline.test.ActualDataRowJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.pipeline.test.ExpectedDataRowJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.pipeline.test.InputDataRowJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.pipeline.test.PipelineStepTestRowJpaRepository;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineStepTestRowRepository;
import com.lombardrisk.ignis.design.server.pipeline.test.model.ActualDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.ExpectedDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.InputDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTestRow;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public class PipelineStepTestRowAdapter implements PipelineStepTestRowRepository {

    private final InputDataRowJpaRepository inputDataRowJpaRepository;
    private final ExpectedDataRowJpaRepository expectedDataRowJpaRepository;
    private final ActualDataRowJpaRepository actualDataRowJpaRepository;
    private final PipelineStepTestRowJpaRepository pipelineStepTestRowJpaRepository;

    public PipelineStepTestRowAdapter(
            final InputDataRowJpaRepository inputDataRowJpaRepository,
            final ExpectedDataRowJpaRepository expectedDataRowJpaRepository,
            final ActualDataRowJpaRepository actualDataRowJpaRepository,
            final PipelineStepTestRowJpaRepository pipelineStepTestRowJpaRepository) {
        this.inputDataRowJpaRepository = inputDataRowJpaRepository;
        this.expectedDataRowJpaRepository = expectedDataRowJpaRepository;
        this.actualDataRowJpaRepository = actualDataRowJpaRepository;
        this.pipelineStepTestRowJpaRepository = pipelineStepTestRowJpaRepository;
    }

    @Override
    public List<InputDataRow> getTestInputRows(final Long testId) {
        return inputDataRowJpaRepository.findAllByPipelineStepTestIdOrderById(testId);
    }

    @Override
    public List<ExpectedDataRow> getTestExpectedRows(final Long testId) {
        return expectedDataRowJpaRepository.findAllByPipelineStepTestIdOrderById(testId);
    }

    @Override
    public List<ActualDataRow> getTestActualRows(final Long testId) {
        return actualDataRowJpaRepository.findAllByPipelineStepTestIdOrderById(testId);
    }

    @Override
    public InputDataRow create(final InputDataRow inputDataRow) {
        return inputDataRowJpaRepository.save(inputDataRow);
    }

    @Override
    public ExpectedDataRow createExpected(final ExpectedDataRow expectedDataRow) {
        return expectedDataRowJpaRepository.save(expectedDataRow);
    }

    @Override
    public PipelineStepTestRow saveRow(final PipelineStepTestRow testRow) {
        return pipelineStepTestRowJpaRepository.save(testRow);
    }

    @Override
    public Optional<PipelineStepTestRow> findById(final Long rowId) {
        return pipelineStepTestRowJpaRepository.findById(rowId);
    }

    @Override
    public void updateMatchingExpectedRows(final List<Long> rowIds) {
        expectedDataRowJpaRepository.updateExpectedRowsStatusAndRun(rowIds, ExpectedDataRow.Status.MATCHED, true);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void updateNotFoundExpectedRows(final List<Long> notFound) {
        expectedDataRowJpaRepository.updateExpectedRowsStatusAndRun(notFound, ExpectedDataRow.Status.NOT_FOUND, true);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void updateInputRowsRunStatus(final List<Long> inputRows, final boolean haveBeenRunThroughTest) {
        inputDataRowJpaRepository.updateRun(inputRows, haveBeenRunThroughTest);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void clearPreviousActualRows(final Long testId) {
        actualDataRowJpaRepository.deleteAllByPipelineStepTestId(testId);
    }

    @Override
    public List<ActualDataRow> saveActualRows(final Iterable<ActualDataRow> actualDataRows) {
        return actualDataRowJpaRepository.saveAll(actualDataRows);
    }

    @Override
    public void deleteInput(final PipelineStepTestRow row) {
        inputDataRowJpaRepository.delete((InputDataRow) row);
    }

    @Override
    public void deleteExpected(final PipelineStepTestRow row) {
        expectedDataRowJpaRepository.delete((ExpectedDataRow) row);
    }

    @Override
    public void deleteRowsById(final Iterable<Long> ids) {
        pipelineStepTestRowJpaRepository.deleteAllByIdIn(ids);
    }
}
