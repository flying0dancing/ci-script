package com.lombardrisk.ignis.server.job.servicerequest;

import com.lombardrisk.ignis.client.external.job.JobStatus;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.search.Filter;
import com.lombardrisk.ignis.data.common.search.FilterOption;
import com.lombardrisk.ignis.data.common.search.FilterValidator;
import com.lombardrisk.ignis.data.common.search.SpecificationBuilder;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import org.springframework.data.jpa.domain.Specification;

public class ServiceRequestSpecificationBuilder extends SpecificationBuilder<ServiceRequest> {

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String START_TIME = "startTime";
    private static final String END_TIME = "endTime";
    private static final String STATUS = "status";
    private static final String SERVICE_REQUEST_TYPE = "serviceRequestType";

    private final TimeSource timeSource;

    public ServiceRequestSpecificationBuilder(final TimeSource timeSource) {
        this.timeSource = timeSource;
    }

    @Override
    protected Validation<CRUDFailure, Specification<ServiceRequest>> build(final Filter filter) {
        String key = filter.getColumnName();
        String value = filter.getFilter();

        if (ID.equals(key)) {
            return FilterValidator.validateNumber(filter);
        }

        if (NAME.equals(key)) {
            return FilterValidator.validateText(filter);
        }

        if (START_TIME.equals(key)) {
            return FilterValidator.validateDate(filter, timeSource.getClock().getZone());
        }

        if (END_TIME.equals(key)) {
            return FilterValidator.validateDate(filter, timeSource.getClock().getZone());
        }

        if (STATUS.equals(key)) {
            return Try.of(() -> JobStatus.valueOf(value.toUpperCase()))
                    .map(status -> statusSpecification(status, filter.getType()))
                    .toValid(invalidValue(key, value));
        }

        if (SERVICE_REQUEST_TYPE.equals(key)) {
            return Try.of(() -> ServiceRequestType.valueOf(value.toUpperCase()))
                    .map(type -> serviceRequestTypeSpecification(type, filter.getType()))
                    .toValid(invalidValue(key, value));
        }

        return Validation.invalid(unknownProperty(filter));
    }

    private Specification<ServiceRequest> statusSpecification(
            final JobStatus status, final FilterOption operator) {

        if (operator == FilterOption.NOT_EQUAL) {
            return (Specification<ServiceRequest>) (root, criteriaQuery, criteriaBuilder) ->
                    criteriaBuilder.notEqual(root.<JobStatus>get(STATUS), status);
        }

        return (Specification<ServiceRequest>) (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.<JobStatus>get(STATUS), status);
    }

    private Specification<ServiceRequest> serviceRequestTypeSpecification(
            final ServiceRequestType type, final FilterOption operator) {

        if (operator == FilterOption.NOT_EQUAL) {
            return (Specification<ServiceRequest>) (root, criteriaQuery, criteriaBuilder) ->
                    criteriaBuilder.notEqual(root.<ServiceRequestType>get(SERVICE_REQUEST_TYPE), type);
        }

        return (Specification<ServiceRequest>) (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.<ServiceRequestType>get(SERVICE_REQUEST_TYPE), type);
    }

    private CRUDFailure unknownProperty(final Filter criteria) {
        String message = String.format("Unknown property '%s'", criteria.getColumnName());
        return CRUDFailure.invalidRequestParameter("search", message);
    }

    private CRUDFailure invalidValue(final String key, final String status) {
        String message = String.format("Invalid value '%s' for key '%s'", status, key);
        return CRUDFailure.invalidRequestParameter("search", message);
    }
}
