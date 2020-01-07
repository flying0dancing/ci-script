package com.lombardrisk.ignis.db.migration.common;

import org.springframework.jdbc.core.JdbcTemplate;

public class MigrationV2053 {

    private static final String FIELD_ID_SEQUENCE = "FIELD_ID_SEQUENCE";
    private static final String STEP_ID_SEQUENCE = "STEP_ID_SEQUENCE";
    private static final String STEP_SELECT_ID_SEQUENCE = "STEP_SELECT_ID_SEQUENCE";

    private static final long INCREMENT_BY_50 = 50L;
    private static final long INCREMENT_BY_30 = 30L;

    private final JdbcTemplate jdbcTemplate;
    private final SequenceSyntax sequenceSyntax;

    public MigrationV2053(final JdbcTemplate jdbcTemplate, final SequenceSyntax sequenceSyntax) {
        this.jdbcTemplate = jdbcTemplate;
        this.sequenceSyntax = sequenceSyntax;
    }

    public void migrate() {
        Long maxFieldId = jdbcTemplate.queryForObject("SELECT MAX(ID)+1 FROM DATASET_SCHEMA_FIELD", Long.class);
        Long maxStepId = jdbcTemplate.queryForObject("SELECT MAX(ID)+1 FROM PIPELINE_STEP", Long.class);
        Long maxStepSelectId = jdbcTemplate.queryForObject("SELECT MAX(ID)+1 FROM PIPELINE_STEP_SELECT", Long.class);

        createSequence(FIELD_ID_SEQUENCE, maxFieldId, INCREMENT_BY_50);
        createSequence(STEP_ID_SEQUENCE, maxStepId, INCREMENT_BY_30);
        createSequence(STEP_SELECT_ID_SEQUENCE, maxStepSelectId, INCREMENT_BY_50);
    }

    private void createSequence(final String sequenceName, final Long maxId, final Long increment) {
        final long startWith = maxId != null ? maxId : 1L;
        String createSequence = sequenceSyntax.createSequence(sequenceName, startWith, increment);
        jdbcTemplate.execute(createSequence);
    }
}
