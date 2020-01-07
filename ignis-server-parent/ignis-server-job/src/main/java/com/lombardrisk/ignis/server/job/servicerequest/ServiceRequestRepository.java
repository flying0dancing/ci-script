package com.lombardrisk.ignis.server.job.servicerequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.transaction.Transactional;

public interface ServiceRequestRepository
        extends JpaRepository<ServiceRequest, Long>, JpaSpecificationExecutor<ServiceRequest> {

    @Transactional
    ServiceRequest findFirstById(final Long id);
}
