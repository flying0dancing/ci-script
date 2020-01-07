package com.lombardrisk.ignis.client.design.pipeline.error;

import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.function.Function;

import static com.lombardrisk.ignis.common.stream.SeqUtils.isNullOrEmpty;

@Data
@Builder
public class StepExecutionResult {

    private final List<ErrorResponse> errors;
    private final SelectsExecutionErrors selectsExecutionErrors;

    public boolean isSuccessful() {
        return isNullOrEmpty(errors) &&
                (selectsExecutionErrors == null ||
                        selectsExecutionErrors.isSuccessful());
    }

    public <T> T fold(
            final Function<StepExecutionResult, T> errorMapper,
            final Function<StepExecutionResult, T> successMapper) {
        return isSuccessful() ? successMapper.apply(this) : errorMapper.apply(this);
    }

    @Data
    @Builder
    public static class SelectsExecutionErrors {

        private final List<ErrorResponse> transformationParseErrors;
        private final List<SelectResult> individualErrors;

        public boolean isSuccessful() {
            return isNullOrEmpty(transformationParseErrors)
                    && isNullOrEmpty(individualErrors);
        }
    }
}
