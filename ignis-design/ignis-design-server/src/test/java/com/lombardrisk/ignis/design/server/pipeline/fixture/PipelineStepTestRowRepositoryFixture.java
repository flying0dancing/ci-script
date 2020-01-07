package com.lombardrisk.ignis.design.server.pipeline.fixture;

import com.lombardrisk.ignis.data.common.fixtures.InMemoryRepository;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineStepTestRowRepository;
import com.lombardrisk.ignis.design.server.pipeline.test.model.ActualDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.ExpectedDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.InputDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTestRow;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public class PipelineStepTestRowRepositoryFixture extends InMemoryRepository<PipelineStepTestRow>
        implements PipelineStepTestRowRepository {

    private final AtomicLong cellIdSequence = new AtomicLong(101);

    @Override
    public PipelineStepTestRow saveRow(final PipelineStepTestRow testRow) {
        return super.save(testRow);
    }

    @Override
    public void updateMatchingExpectedRows(final List<Long> rowIds) {
        findAllByIds(rowIds).stream()
                .filter(pipelineStepTestRow -> pipelineStepTestRow.getType().equals(PipelineStepTestRow.Type.EXPECTED))
                .map(ExpectedDataRow.class::cast)
                .forEach(pipelineStepTestRow -> {
                    pipelineStepTestRow.setStatus(ExpectedDataRow.Status.MATCHED);
                    pipelineStepTestRow.setRun(true);
                });
    }

    @Override
    public void updateNotFoundExpectedRows(final List<Long> notFound) {
        findAllByIds(notFound).stream()
                .filter(pipelineStepTestRow -> pipelineStepTestRow.getType().equals(PipelineStepTestRow.Type.EXPECTED))
                .map(ExpectedDataRow.class::cast)
                .forEach(pipelineStepTestRow -> {
                    pipelineStepTestRow.setStatus(ExpectedDataRow.Status.NOT_FOUND);
                    pipelineStepTestRow.setRun(true);
                });
    }

    @Override
    public void updateInputRowsRunStatus(final List<Long> inputRows, final boolean haveBeenRunThroughTest) {
        findAllByIds(inputRows).stream()
                .filter(pipelineStepTestRow -> pipelineStepTestRow.getType().equals(PipelineStepTestRow.Type.INPUT))
                .forEach(row -> row.setRun(haveBeenRunThroughTest));

    }

    @Override
    public void clearPreviousActualRows(final Long testId) {
        List<PipelineStepTestRow> rowsToDelete = findAll().stream()
                .filter(row -> row.getType().equals(PipelineStepTestRow.Type.ACTUAL))
                .filter(row -> row.getPipelineStepTestId().equals(testId))
                .collect(Collectors.toList());

        deleteAll(rowsToDelete);
    }

    @Override
    public List<ActualDataRow> saveActualRows(final Iterable<ActualDataRow> actualDataRows) {
        return saveAll(actualDataRows);
    }

    @Override
    public List<InputDataRow> getTestInputRows(final Long testId) {
        return getTestRowsByTestId(testId)
                .filter(row -> row.getType() == PipelineStepTestRow.Type.INPUT)
                .map(InputDataRow.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpectedDataRow> getTestExpectedRows(final Long testId) {
        return getTestRowsByTestId(testId)
                .filter(row -> row.getType() == PipelineStepTestRow.Type.EXPECTED)
                .map(ExpectedDataRow.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<ActualDataRow> getTestActualRows(final Long testId) {
        return getTestRowsByTestId(testId)
                .filter(row -> row.getType() == PipelineStepTestRow.Type.ACTUAL)
                .map(ActualDataRow.class::cast)
                .collect(Collectors.toList());
    }

    private Stream<PipelineStepTestRow> getTestRowsByTestId(final Long testId) {
        return findAll().stream()
                .filter(row -> testId.equals(row.getPipelineStepTestId()));
    }

    @Override
    public InputDataRow create(final InputDataRow inputDataRow) {
        inputDataRow.getCells()
                .forEach(cell -> cell.setId(cellIdSequence.getAndIncrement()));

        return super.save(inputDataRow);
    }

    @Override
    public ExpectedDataRow createExpected(final ExpectedDataRow expectedDataRow) {
        expectedDataRow.getCells()
                .forEach(cell -> cell.setId(cellIdSequence.getAndIncrement()));

        return super.save(expectedDataRow);
    }

    @Override
    public void deleteInput(final PipelineStepTestRow row) {
        deleteById(row.getId());
    }

    @Override
    public void deleteExpected(final PipelineStepTestRow row) {
        deleteById(row.getId());
    }

    @Override
    public void deleteRowsById(final Iterable<Long> ids) {
        ids.forEach(super::deleteById);
    }
}
