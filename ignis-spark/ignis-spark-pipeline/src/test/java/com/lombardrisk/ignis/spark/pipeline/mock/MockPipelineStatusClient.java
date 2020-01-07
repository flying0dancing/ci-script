package com.lombardrisk.ignis.spark.pipeline.mock;

import com.lombardrisk.ignis.client.core.view.IdView;
import com.lombardrisk.ignis.client.internal.PipelineStatusClient;
import com.lombardrisk.ignis.client.internal.UpdatePipelineStepStatusRequest;
import com.lombardrisk.ignis.spark.core.mock.RetrofitCall;
import lombok.Getter;
import retrofit2.Call;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class MockPipelineStatusClient implements PipelineStatusClient {

    private final Map<Long, List<UpdatePipelineStepStatusRequest>> requests = new HashMap<>();

    @Override
    public Call<IdView> updatePipelineStepInvocationStatus(
            final long pipelineInvocationId,
            final UpdatePipelineStepStatusRequest request) {

        requests.computeIfAbsent(pipelineInvocationId, k -> new ArrayList<>()).add(request);

        return new RetrofitCall<>(new IdView(123L));
    }

    public void reset() {
        requests.clear();
    }

    public List<UpdatePipelineStepStatusRequest> getRequestsByPipelineInvocationId(final Long invocationId) {
        return requests.get(invocationId);
    }
}
