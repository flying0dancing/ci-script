package com.lombardrisk.ignis.server.job.servicerequest;

import com.lombardrisk.ignis.client.external.job.ExternalExitStatus;
import com.lombardrisk.ignis.client.external.job.JobStatus;
import com.lombardrisk.ignis.data.common.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Date;

/**
 * Service Request entity has one to one relationship with Spring batch execution.
 * Used to avoid tight coupling with Spring batch
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "SVC_SERVICE_REQUEST")
@ToString
public class ServiceRequest implements Identifiable {

    public static final String SERVICE_REQUEST_ID = "serviceRequestId";
    public static final String CORRELATION_ID = "correlationId";
    public static final String PRODUCT_CONFIG_ID = "productConfigId";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "REQUEST_MESSAGE")
    @Lob
    private String requestMessage;

    //Spring batch id
    @Column(name = "JOB_EXECUTION_ID")
    private Long jobExecutionId;

    @Column(name = "YARN_APPLICATION_TRACKING_URL")
    private String trackingUrl;

    @Column(name = "REQUEST_TYPE")
    @Enumerated(EnumType.STRING)
    private ServiceRequestType serviceRequestType;

    @Column(name = "START_TIME")
    private Date startTime;

    @Column(name = "END_TIME")
    private Date endTime;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private JobStatus status = JobStatus.STARTING;

    @Column(name = "EXIT_CODE")
    @Enumerated(EnumType.STRING)
    private ExternalExitStatus exitCode = ExternalExitStatus.UNKNOWN;

    @SuppressWarnings({"unused", "squid:S1068"})
    public static class ServiceRequestBuilder {
        private JobStatus status = JobStatus.STARTING;
        private ExternalExitStatus getExitCode = ExternalExitStatus.UNKNOWN;
    }
}
