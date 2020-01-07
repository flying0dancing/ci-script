package com.lombardrisk.ignis.server.jpa;

import com.lombardrisk.ignis.client.external.job.ExternalExitStatus;
import com.lombardrisk.ignis.client.external.job.JobStatus;
import com.lombardrisk.ignis.data.common.search.Filter;
import com.lombardrisk.ignis.data.common.search.FilterOption;
import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.job.fixture.JobPopulated;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestRepository;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestSpecificationBuilder;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import static com.lombardrisk.ignis.data.common.search.CombinedFilter.and;
import static com.lombardrisk.ignis.data.common.search.CombinedFilter.or;
import static com.lombardrisk.ignis.data.common.search.FilterOption.ENDS_WITH;
import static com.lombardrisk.ignis.data.common.search.FilterOption.EQUALS;
import static com.lombardrisk.ignis.data.common.search.FilterOption.GREATER_THAN;
import static com.lombardrisk.ignis.data.common.search.FilterOption.GREATER_THAN_OR_EQUAL;
import static com.lombardrisk.ignis.data.common.search.FilterOption.IN_RANGE;
import static com.lombardrisk.ignis.data.common.search.FilterOption.LESS_THAN;
import static com.lombardrisk.ignis.data.common.search.FilterOption.LESS_THAN_OR_EQUAL;
import static com.lombardrisk.ignis.data.common.search.FilterOption.NOT_CONTAINS;
import static com.lombardrisk.ignis.data.common.search.FilterOption.NOT_EQUAL;
import static com.lombardrisk.ignis.data.common.search.FilterOption.STARTS_WITH;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType.IMPORT_PRODUCT;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType.PIPELINE;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType.ROLLBACK_PRODUCT;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType.STAGING;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType.VALIDATION;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.tuple;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
@SuppressWarnings("unchecked")
public class ServiceRequestRepositoryIT {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Autowired
    private ServiceRequestRepository repository;

    @Autowired
    private ServiceRequestSpecificationBuilder specificationBuilder;

    @Test
    public void saveAndFindAll() {
        ServiceRequest serviceRequest1 = JobPopulated.stagingJobServiceRequest()
                .name("Job 1")
                .createdBy("user1")
                .requestMessage("message 1")
                .trackingUrl("http://trackthis/1")
                .serviceRequestType(STAGING)
                .startTime(toDate("2019-01-01"))
                .endTime(toDate("2019-01-01"))
                .status(JobStatus.STARTED)
                .exitCode(ExternalExitStatus.UNKNOWN)
                .build();

        ServiceRequest serviceRequest2 = JobPopulated.stagingJobServiceRequest()
                .name("Job 2")
                .createdBy("user2")
                .requestMessage("message 2")
                .trackingUrl("http://trackthis/2")
                .serviceRequestType(VALIDATION)
                .startTime(toDate("2019-01-02"))
                .endTime(toDate("2019-01-02"))
                .status(JobStatus.STOPPING)
                .exitCode(ExternalExitStatus.STOPPED)
                .build();

        ServiceRequest serviceRequest3 = JobPopulated.stagingJobServiceRequest()
                .name("Job 3")
                .createdBy("user3")
                .requestMessage("message 3")
                .trackingUrl("http://trackthis/3")
                .serviceRequestType(PIPELINE)
                .startTime(toDate("2019-01-03"))
                .endTime(toDate("2019-01-03"))
                .status(JobStatus.COMPLETED)
                .exitCode(ExternalExitStatus.FAILED)
                .build();

        repository.saveAll(asList(serviceRequest1, serviceRequest2, serviceRequest3));

        List<ServiceRequest> serviceRequests = repository
                .findAll(PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "name")))
                .getContent();

        soft.assertThat(serviceRequests)
                .hasSize(3);

        soft.assertThat(serviceRequests)
                .extracting(ServiceRequest::getId)
                .doesNotContainNull()
                .doesNotHaveDuplicates();

        soft.assertThat(serviceRequests.get(0))
                .isEqualToIgnoringGivenFields(serviceRequest1, "id");

        soft.assertThat(serviceRequests.get(1))
                .isEqualToIgnoringGivenFields(serviceRequest2, "id");

        soft.assertThat(serviceRequests.get(2))
                .isEqualToIgnoringGivenFields(serviceRequest3, "id");
    }

    @Test
    public void findAll_IdSpecification() {
        ServiceRequest serviceRequest1 = repository.save(JobPopulated.stagingJobServiceRequest().build());
        ServiceRequest serviceRequest2 = repository.save(JobPopulated.stagingJobServiceRequest().build());
        ServiceRequest serviceRequest3 = repository.save(JobPopulated.stagingJobServiceRequest().build());
        ServiceRequest serviceRequest4 = repository.save(JobPopulated.stagingJobServiceRequest().build());
        ServiceRequest serviceRequest5 = repository.save(JobPopulated.stagingJobServiceRequest().build());

        Filter idEquals1 = Filter.builder()
                .columnName("id")
                .type(EQUALS)
                .filter(String.valueOf(serviceRequest1.getId()))
                .build();
        Filter idEquals3 = Filter.builder()
                .columnName("id")
                .type(EQUALS)
                .filter(String.valueOf(serviceRequest3.getId()))
                .build();
        Filter idLessThan3 = Filter.builder()
                .columnName("id")
                .type(LESS_THAN)
                .filter(String.valueOf(serviceRequest3.getId()))
                .build();
        Filter idBetween3And5 = Filter.builder()
                .columnName("id")
                .type(IN_RANGE)
                .filter(String.valueOf(serviceRequest3.getId()))
                .filterTo(String.valueOf(serviceRequest5.getId()))
                .build();

        soft.assertThat(
                repository.findAll(specificationBuilder.build(idEquals3).get()))
                .containsExactlyInAnyOrder(serviceRequest3);

        soft.assertThat(
                repository.findAll(specificationBuilder.build(idLessThan3).get()))
                .containsExactlyInAnyOrder(serviceRequest1, serviceRequest2);

        soft.assertThat(
                repository.findAll(specificationBuilder.build(idBetween3And5).get()))
                .containsExactlyInAnyOrder(serviceRequest3, serviceRequest4, serviceRequest5);

        soft.assertThat(
                repository.findAll(specificationBuilder.build(or(idEquals1, idBetween3And5)).get()))
                .containsExactlyInAnyOrder(serviceRequest1, serviceRequest3, serviceRequest4, serviceRequest5);
    }

    @Test
    public void findAll_NameSpecification() {
        ServiceRequest serviceRequest1 = repository.save(JobPopulated.stagingJobServiceRequest()
                .name("this is a dog")
                .build());
        ServiceRequest serviceRequest2 = repository.save(JobPopulated.stagingJobServiceRequest()
                .name("this is a cat")
                .build());
        ServiceRequest serviceRequest3 = repository.save(JobPopulated.stagingJobServiceRequest()
                .name("this is another dog")
                .build());
        ServiceRequest serviceRequest4 = repository.save(JobPopulated.stagingJobServiceRequest()
                .name("this is some other animal")
                .build());
        ServiceRequest serviceRequest5 = repository.save(JobPopulated.stagingJobServiceRequest()
                .name("this is a dog")
                .build());

        Filter nameEqualsSomeOtherAnimal = Filter.builder()
                .columnName("name").type(EQUALS).filter("this is some other animal").build();

        Filter nameNotEqualSomeOtherAnimal = Filter.builder()
                .columnName("name").type(NOT_EQUAL).filter("this is some other animal").build();

        Filter nameContainsLetterA = Filter.builder()
                .columnName("name").type(FilterOption.CONTAINS).filter(" a ").build();

        Filter nameDoesNotContainDog = Filter.builder()
                .columnName("name").type(NOT_CONTAINS).filter("dog").build();

        Filter nameStartsWithThisIsA = Filter.builder()
                .columnName("name").type(STARTS_WITH).filter("this is a").build();

        Filter nameEndsWithRDog = Filter.builder()
                .columnName("name").type(ENDS_WITH).filter("r dog").build();

        Filter nameEndsWithCat = Filter.builder()
                .columnName("name").type(ENDS_WITH).filter("cat").build();

        soft.assertThat(
                repository.findAll(specificationBuilder.build(nameEqualsSomeOtherAnimal).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getName)
                .containsExactlyInAnyOrder(tuple(serviceRequest4.getId(), "this is some other animal"));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(nameNotEqualSomeOtherAnimal).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getName)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest1.getId(), "this is a dog"),
                        tuple(serviceRequest2.getId(), "this is a cat"),
                        tuple(serviceRequest3.getId(), "this is another dog"),
                        tuple(serviceRequest5.getId(), "this is a dog"));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(nameContainsLetterA).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getName)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest1.getId(), "this is a dog"),
                        tuple(serviceRequest2.getId(), "this is a cat"),
                        tuple(serviceRequest5.getId(), "this is a dog"));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(nameDoesNotContainDog).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getName)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest2.getId(), "this is a cat"),
                        tuple(serviceRequest4.getId(), "this is some other animal"));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(nameStartsWithThisIsA).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getName)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest1.getId(), "this is a dog"),
                        tuple(serviceRequest2.getId(), "this is a cat"),
                        tuple(serviceRequest3.getId(), "this is another dog"),
                        tuple(serviceRequest5.getId(), "this is a dog"));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(nameEndsWithRDog).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getName)
                .containsExactlyInAnyOrder(tuple(serviceRequest3.getId(), "this is another dog"));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(
                        or(nameEqualsSomeOtherAnimal, nameDoesNotContainDog)).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getName)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest2.getId(), "this is a cat"),
                        tuple(serviceRequest4.getId(), "this is some other animal"));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(
                        and(nameStartsWithThisIsA, nameEndsWithRDog)).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getName)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest3.getId(), "this is another dog"));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(
                        and(nameStartsWithThisIsA, or(nameEndsWithCat, nameEndsWithRDog))).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getName)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest2.getId(), "this is a cat"),
                        tuple(serviceRequest3.getId(), "this is another dog"));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(
                        and(or(nameEndsWithRDog, nameEndsWithCat), nameStartsWithThisIsA)).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getName)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest2.getId(), "this is a cat"),
                        tuple(serviceRequest3.getId(), "this is another dog"));
    }

    @Test
    public void findAll_StartTimeSpecification() {
        Date date1 = toDate("2019-01-01");
        Date date2 = toDate("2019-01-02");
        Date date3 = toDate("2018-12-31");
        Date date4 = toDate("2019-01-03");

        ServiceRequest serviceRequest1 = repository.save(JobPopulated.stagingJobServiceRequest()
                .startTime(date1)
                .build());
        ServiceRequest serviceRequest2 = repository.save(JobPopulated.stagingJobServiceRequest()
                .startTime(date1)
                .build());
        ServiceRequest serviceRequest3 = repository.save(JobPopulated.stagingJobServiceRequest()
                .startTime(date2)
                .build());
        ServiceRequest serviceRequest4 = repository.save(JobPopulated.stagingJobServiceRequest()
                .startTime(date3)
                .build());
        ServiceRequest serviceRequest5 = repository.save(JobPopulated.stagingJobServiceRequest()
                .startTime(date4)
                .build());

        Filter equals = Filter.builder()
                .columnName("startTime").type(EQUALS).dateFrom("2019-01-01").build();

        Filter notEquals = Filter.builder()
                .columnName("startTime").type(NOT_EQUAL).dateFrom("2019-01-01").build();

        Filter lessThan = Filter.builder()
                .columnName("startTime").type(LESS_THAN).dateFrom("2019-01-01").build();

        Filter lessThanOrEqual = Filter.builder()
                .columnName("startTime").type(LESS_THAN_OR_EQUAL).dateFrom("2019-01-01").build();

        Filter greaterThan = Filter.builder()
                .columnName("startTime").type(GREATER_THAN).dateFrom("2019-01-01").build();

        Filter greaterThanOrEqual = Filter.builder()
                .columnName("startTime").type(GREATER_THAN_OR_EQUAL).dateFrom("2019-01-01").build();

        Filter between = Filter.builder()
                .columnName("startTime").type(IN_RANGE)
                .dateFrom("2019-01-01").dateTo("2019-01-03")
                .build();

        soft.assertThat(
                repository.findAll(specificationBuilder.build(equals).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getStartTime)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest1.getId(), date1),
                        tuple(serviceRequest2.getId(), date1));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(notEquals).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getStartTime)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest3.getId(), date2),
                        tuple(serviceRequest4.getId(), date3),
                        tuple(serviceRequest5.getId(), date4));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(lessThan).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getStartTime)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest4.getId(), date3));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(lessThanOrEqual).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getStartTime)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest1.getId(), date1),
                        tuple(serviceRequest2.getId(), date1),
                        tuple(serviceRequest4.getId(), date3));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(greaterThan).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getStartTime)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest3.getId(), date2),
                        tuple(serviceRequest5.getId(), date4));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(greaterThanOrEqual).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getStartTime)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest1.getId(), date1),
                        tuple(serviceRequest2.getId(), date1),
                        tuple(serviceRequest3.getId(), date2),
                        tuple(serviceRequest5.getId(), date4));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(between).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getStartTime)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest1.getId(), date1),
                        tuple(serviceRequest2.getId(), date1),
                        tuple(serviceRequest3.getId(), date2),
                        tuple(serviceRequest5.getId(), date4));
    }

    @Test
    public void findAll_EndTimeSpecification() {
        Date date1 = toDate("2019-01-01");
        Date date2 = toDate("2019-01-02");
        Date date3 = toDate("2018-12-31");
        Date date4 = toDate("2019-01-03");

        ServiceRequest serviceRequest1 = repository.save(JobPopulated.stagingJobServiceRequest()
                .endTime(date1)
                .build());
        ServiceRequest serviceRequest2 = repository.save(JobPopulated.stagingJobServiceRequest()
                .endTime(date1)
                .build());
        ServiceRequest serviceRequest3 = repository.save(JobPopulated.stagingJobServiceRequest()
                .endTime(date2)
                .build());
        ServiceRequest serviceRequest4 = repository.save(JobPopulated.stagingJobServiceRequest()
                .endTime(date3)
                .build());
        ServiceRequest serviceRequest5 = repository.save(JobPopulated.stagingJobServiceRequest()
                .endTime(date4)
                .build());

        Filter equals = Filter.builder()
                .columnName("endTime").type(EQUALS).dateFrom("2019-01-01").build();

        Filter notEquals = Filter.builder()
                .columnName("endTime").type(NOT_EQUAL).dateFrom("2019-01-01").build();

        Filter lessThan = Filter.builder()
                .columnName("endTime").type(LESS_THAN).dateFrom("2019-01-01").build();

        Filter lessThanOrEqual = Filter.builder()
                .columnName("endTime").type(LESS_THAN_OR_EQUAL).dateFrom("2019-01-01").build();

        Filter greaterThan = Filter.builder()
                .columnName("endTime").type(GREATER_THAN).dateFrom("2019-01-01").build();

        Filter greaterThanOrEqual = Filter.builder()
                .columnName("endTime").type(GREATER_THAN_OR_EQUAL).dateFrom("2019-01-01").build();

        Filter between = Filter.builder()
                .columnName("endTime").type(IN_RANGE)
                .dateFrom("2019-01-01").dateTo("2019-01-03")
                .build();

        soft.assertThat(
                repository.findAll(specificationBuilder.build(equals).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getEndTime)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest1.getId(), date1),
                        tuple(serviceRequest2.getId(), date1));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(notEquals).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getEndTime)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest3.getId(), date2),
                        tuple(serviceRequest4.getId(), date3),
                        tuple(serviceRequest5.getId(), date4));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(lessThan).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getEndTime)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest4.getId(), date3));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(lessThanOrEqual).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getEndTime)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest1.getId(), date1),
                        tuple(serviceRequest2.getId(), date1),
                        tuple(serviceRequest4.getId(), date3));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(greaterThan).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getEndTime)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest3.getId(), date2),
                        tuple(serviceRequest5.getId(), date4));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(greaterThanOrEqual).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getEndTime)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest1.getId(), date1),
                        tuple(serviceRequest2.getId(), date1),
                        tuple(serviceRequest3.getId(), date2),
                        tuple(serviceRequest5.getId(), date4));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(between).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getEndTime)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest1.getId(), date1),
                        tuple(serviceRequest2.getId(), date1),
                        tuple(serviceRequest3.getId(), date2),
                        tuple(serviceRequest5.getId(), date4));
    }

    @Test
    public void findAll_StatusSpecification() {
        ServiceRequest serviceRequest1 = repository.save(JobPopulated.stagingJobServiceRequest()
                .status(JobStatus.COMPLETED)
                .build());
        ServiceRequest serviceRequest2 = repository.save(JobPopulated.stagingJobServiceRequest()
                .status(JobStatus.STARTING)
                .build());
        ServiceRequest serviceRequest3 = repository.save(JobPopulated.stagingJobServiceRequest()
                .status(JobStatus.STARTED)
                .build());
        ServiceRequest serviceRequest4 = repository.save(JobPopulated.stagingJobServiceRequest()
                .status(JobStatus.COMPLETED)
                .build());

        Filter statusCompleted = Filter.builder()
                .columnName("status").type(EQUALS).filter("completed").build();

        Filter statusNotCompleted = Filter.builder()
                .columnName("status").type(NOT_EQUAL).filter("COMPLETED").build();

        Filter statusStarting = Filter.builder()
                .columnName("status").type(EQUALS).filter("STARTING").build();

        Filter statusStarted = Filter.builder()
                .columnName("status").type(EQUALS).filter("STARTED").build();

        soft.assertThat(
                repository.findAll(specificationBuilder.build(statusCompleted).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getStatus)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest1.getId(), JobStatus.COMPLETED),
                        tuple(serviceRequest4.getId(), JobStatus.COMPLETED));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(statusNotCompleted).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getStatus)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest2.getId(), JobStatus.STARTING),
                        tuple(serviceRequest3.getId(), JobStatus.STARTED));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(statusStarting).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getStatus)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest2.getId(), JobStatus.STARTING));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(
                        or(statusCompleted, statusStarted)).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getStatus)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest1.getId(), JobStatus.COMPLETED),
                        tuple(serviceRequest3.getId(), JobStatus.STARTED),
                        tuple(serviceRequest4.getId(), JobStatus.COMPLETED));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(
                        or(statusCompleted, statusStarted, statusStarting)).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getStatus)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest1.getId(), JobStatus.COMPLETED),
                        tuple(serviceRequest2.getId(), JobStatus.STARTING),
                        tuple(serviceRequest3.getId(), JobStatus.STARTED),
                        tuple(serviceRequest4.getId(), JobStatus.COMPLETED));
    }

    @Test
    public void findAll_ServiceRequestTypeSpecification() {
        ServiceRequest serviceRequest1 = repository.save(JobPopulated.stagingJobServiceRequest()
                .serviceRequestType(STAGING)
                .build());
        ServiceRequest serviceRequest2 = repository.save(JobPopulated.stagingJobServiceRequest()
                .serviceRequestType(VALIDATION)
                .build());
        ServiceRequest serviceRequest3 = repository.save(JobPopulated.stagingJobServiceRequest()
                .serviceRequestType(PIPELINE)
                .build());
        ServiceRequest serviceRequest4 = repository.save(JobPopulated.stagingJobServiceRequest()
                .serviceRequestType(IMPORT_PRODUCT)
                .build());
        ServiceRequest serviceRequest5 = repository.save(JobPopulated.stagingJobServiceRequest()
                .serviceRequestType(ROLLBACK_PRODUCT)
                .build());

        Filter staging = Filter.builder()
                .columnName("serviceRequestType").type(EQUALS).filter("staging").build();

        Filter notStaging = Filter.builder()
                .columnName("serviceRequestType").type(NOT_EQUAL).filter("STAGING").build();

        Filter pipeline = Filter.builder()
                .columnName("serviceRequestType").type(EQUALS).filter("PIPELINE").build();

        soft.assertThat(
                repository.findAll(specificationBuilder.build(staging).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getServiceRequestType)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest1.getId(), STAGING));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(or(staging, pipeline)).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getServiceRequestType)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest1.getId(), STAGING),
                        tuple(serviceRequest3.getId(), PIPELINE));

        soft.assertThat(
                repository.findAll(specificationBuilder.build(notStaging).get()))
                .extracting(ServiceRequest::getId, ServiceRequest::getServiceRequestType)
                .containsExactlyInAnyOrder(
                        tuple(serviceRequest2.getId(), VALIDATION),
                        tuple(serviceRequest3.getId(), PIPELINE),
                        tuple(serviceRequest4.getId(), IMPORT_PRODUCT),
                        tuple(serviceRequest5.getId(), ROLLBACK_PRODUCT));
    }

    private static Date toDate(final String date) {
        ZonedDateTime localDateTime = LocalDate
                .parse(date, DateTimeFormatter.ISO_DATE)
                .atStartOfDay(ZoneId.systemDefault());

        return Timestamp.from(localDateTime.toInstant());
    }
}


