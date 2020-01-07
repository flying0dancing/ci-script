package com.lombardrisk.ignis.design.server.pipeline.api;

import com.lombardrisk.ignis.design.server.pipeline.test.model.ActualDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.ExpectedDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.InputDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTestRow;

import java.util.List;
import java.util.Optional;

public interface PipelineStepTestRowRepository {

    List<InputDataRow> getTestInputRows(Long testId);

    List<ExpectedDataRow> getTestExpectedRows(Long testId);

    List<ActualDataRow> getTestActualRows(Long testId);

    InputDataRow create(InputDataRow inputDataRow);
    ExpectedDataRow createExpected(ExpectedDataRow expectedDataRow);

    PipelineStepTestRow saveRow(PipelineStepTestRow testRow);

    Optional<PipelineStepTestRow> findById(Long rowId);

    void updateMatchingExpectedRows(List<Long> rowIds);

    void updateNotFoundExpectedRows(List<Long> notFound);

    void updateInputRowsRunStatus(List<Long> inputRows, boolean haveBeenRunThroughTest);

    void clearPreviousActualRows(Long testId);

    List<ActualDataRow> saveActualRows(Iterable<ActualDataRow> actualDataRows);

    void deleteInput(PipelineStepTestRow row);

    void deleteExpected(PipelineStepTestRow row);

    void deleteRowsById(Iterable<Long> ids);
}
