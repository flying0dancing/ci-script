package com.lombardrisk.ignis.server.dataset.rule.detail;

import com.lombardrisk.ignis.server.dataset.phoenix.PhoenixResultRowMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

public class ValidationDetailRepository {

    private final JdbcTemplate jdbcTemplate;

    public ValidationDetailRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Finds validation details for a dataset and rule(s).
     * Counts are estimated to be dataset.recordsCount * validationRules.size() as they are expensive to obtain.
     *
     * @param query Validation Query object
     * @return Paginated list of rows
     */
    public Page<ValidationDetailRow> findValidationDetails(final ValidationDetailQuery query) {
        List<Map<String, Object>> rows = jdbcTemplate.query(
                query.getSqlQuery(), PhoenixResultRowMapper.fromFields(query.getValidationDetailsColumns()));

        return new PageImpl<>(rows, query.getPageRequest(), query.getRecordsCount())
                .map(ValidationDetailRow::new);
    }
}
