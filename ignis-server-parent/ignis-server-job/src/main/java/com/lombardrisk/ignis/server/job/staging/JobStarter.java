package com.lombardrisk.ignis.server.job.staging;

import com.lombardrisk.ignis.server.job.exception.JobStartException;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;

import java.util.Properties;

public interface JobStarter {
    long startJob(
            String jobName,
            ServiceRequest serviceRequest,
            Properties properties) throws JobStartException;
}
