package com.lombardrisk.ignis.server.dataset.pipeline.invocation;

import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineInvocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PipelineInvocationJpaRepository extends JpaRepository<PipelineInvocation, Long> {

    List<PipelineInvocation> findAllByServiceRequestId(Long serviceRequestId);

    void deleteAllByPipelineId(Long id);
}
