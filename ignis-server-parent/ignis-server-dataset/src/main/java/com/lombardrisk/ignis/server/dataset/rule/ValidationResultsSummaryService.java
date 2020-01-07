package com.lombardrisk.ignis.server.dataset.rule;

import com.google.common.collect.Sets;
import com.lombardrisk.ignis.api.rule.SummaryStatus;
import com.lombardrisk.ignis.client.internal.RuleSummaryRequest;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.dataset.rule.model.ValidationResultsSummary;
import com.lombardrisk.ignis.server.product.rule.ValidationRuleRepository;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import io.vavr.control.Either;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

public class ValidationResultsSummaryService {

    private final ValidationRuleRepository validationRuleRepository;
    private final ValidationResultsSummaryRepository validationResultsSummaryRepository;
    private final TimeSource timeSource;

    public ValidationResultsSummaryService(
            final ValidationRuleRepository validationRuleRepository,
            final ValidationResultsSummaryRepository validationResultsSummaryRepository,
            final TimeSource timeSource) {
        this.validationRuleRepository = validationRuleRepository;
        this.validationResultsSummaryRepository = validationResultsSummaryRepository;
        this.timeSource = timeSource;
    }

    public List<ValidationResultsSummary> findByDatasetId(final long datasetId) {
        return validationResultsSummaryRepository.findByDatasetId(datasetId);
    }

    @Transactional
    public Either<CRUDFailure, List<ValidationResultsSummary>> create(
            final Dataset dataset,
            final List<RuleSummaryRequest> ruleSummaryRequests) {
        validationResultsSummaryRepository.deleteAllByDataset(dataset);

        Set<Long> ruleIds = ruleSummaryRequests.stream()
                .map(RuleSummaryRequest::getRuleId)
                .collect(toSet());

        Either<CRUDFailure, Map<Long, ValidationRule>> validationRules = findValidationRulesOrIdsNotFound(ruleIds)
                .mapLeft(ids -> CRUDFailure.notFoundIds(ValidationRule.class.getSimpleName(), ids));

        if (validationRules.isRight()) {
            return Either.right(createNewRuleSummaries(validationRules.get(), dataset, ruleSummaryRequests));
        }

        return Either.left(validationRules.getLeft());
    }

    private List<ValidationResultsSummary> createNewRuleSummaries(
            final Map<Long, ValidationRule> idValidationRuleMap,
            final Dataset dataset,
            final List<RuleSummaryRequest> ruleSummaryRequests) {

        List<ValidationResultsSummary> summaries = new ArrayList<>();
        for (final RuleSummaryRequest ruleSummaryRequest : ruleSummaryRequests) {

            ValidationRule validationRule = idValidationRuleMap.get(ruleSummaryRequest.getRuleId());
            SummaryStatus status = ruleSummaryRequest.getStatus();

            ValidationResultsSummary.ValidationResultsSummaryBuilder ruleSummaryBuilder =
                    ValidationResultsSummary.builder()
                            .dataset(dataset)
                            .validationRule(validationRule)
                            .createdTime(timeSource.nowAsDate())
                            .status(status)
                            .errorMessage(ruleSummaryRequest.getErrorMessage())
                            .numberOfFailures(ruleSummaryRequest.numberOfFailures().getOrNull())
                            .numberOfErrors(ruleSummaryRequest.numberOfErrors().getOrNull());

            summaries.add(ruleSummaryBuilder.build());
        }

        return validationResultsSummaryRepository.saveAll(summaries);
    }

    private Either<Set<Long>, Map<Long, ValidationRule>> findValidationRulesOrIdsNotFound(final Set<Long> ruleIds) {
        Map<Long, ValidationRule> rules = validationRuleRepository.findAllById(ruleIds)
                .stream()
                .collect(Collectors.toMap(ValidationRule::getId, Function.identity()));

        Set<Long> idsNotFound = Sets.difference(ruleIds, rules.keySet())
                .immutableCopy();

        if (!idsNotFound.isEmpty()) {
            return Either.left(idsNotFound);
        }

        return Either.right(rules);
    }
}
