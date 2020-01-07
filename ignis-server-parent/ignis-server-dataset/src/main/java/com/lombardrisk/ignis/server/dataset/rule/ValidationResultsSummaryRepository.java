package com.lombardrisk.ignis.server.dataset.rule;

import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.dataset.rule.model.ValidationResultsSummary;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ValidationResultsSummaryRepository extends JpaRepository<ValidationResultsSummary, Long> {

    List<ValidationResultsSummary> findByDatasetId(final long datasetId);

    void deleteAllByDataset(final Dataset dataset);

    void deleteAllByValidationRule(final ValidationRule validationRule);
}
