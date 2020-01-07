package com.lombardrisk.ignis.db.migration.oracle;

import com.lombardrisk.ignis.TestFlywayApplication;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.server.product.pipeline.select.Select;
import com.lombardrisk.ignis.server.product.table.model.Field;
import com.lombardrisk.ignis.util.PipelineUtil;
import com.lombardrisk.ignis.util.ProductConfigUtil;
import com.lombardrisk.ignis.util.TableUtil;
import org.flywaydb.core.Flyway;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestFlywayApplication.class)
@TestPropertySource(properties = { "spring.flyway.target=2052" })
@SuppressWarnings("squid:S00101")
public class V2053__Improve_Product_Config_Import_Performance_IT {

    private static final String NEXT_FIELD_ID_FROM_SEQ = "select FIELD_ID_SEQUENCE.nextval from dual";
    private static final String NEXT_STEP_ID_FROM_SEQ = "select STEP_ID_SEQUENCE.nextval from dual";
    private static final String NEXT_SELECT_ID_FROM_SEQ = "select STEP_SELECT_ID_SEQUENCE.nextval from dual";

    @Autowired
    private Flyway flyway;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ProductConfigUtil productConfigUtil;
    @Autowired
    private TableUtil tableUtil;
    @Autowired
    private PipelineUtil pipelineUtil;

    @Before
    public void setUp() {
        flyway.migrate();

        productConfigUtil.save(ProductPopulated.productConfig().id(1L).build());
    }

    @After
    public void tearDown() {
        flyway.clean();
        flyway.setTargetAsString("2052");
    }

    private void migrateDatabase() {
        flyway.setTargetAsString("2053");
        flyway.migrate();
    }

    @Test
    public void migrate_EmptyDatabase_SetsSequenceValuesToOne() {
        migrateDatabase();

        assertThat(jdbcTemplate.queryForObject(NEXT_FIELD_ID_FROM_SEQ, Long.class)).isEqualTo(1L);
        assertThat(jdbcTemplate.queryForObject(NEXT_FIELD_ID_FROM_SEQ, Long.class)).isEqualTo(51L);

        assertThat(jdbcTemplate.queryForObject(NEXT_STEP_ID_FROM_SEQ, Long.class)).isEqualTo(1L);
        assertThat(jdbcTemplate.queryForObject(NEXT_STEP_ID_FROM_SEQ, Long.class)).isEqualTo(31L);

        assertThat(jdbcTemplate.queryForObject(NEXT_SELECT_ID_FROM_SEQ, Long.class)).isEqualTo(1L);
        assertThat(jdbcTemplate.queryForObject(NEXT_SELECT_ID_FROM_SEQ, Long.class)).isEqualTo(51L);
    }

    @Test
    public void migrate_WithExistingFields_SetsNextFieldId() {
        Field field1 = ProductPopulated.stringField("string").id(3L).build();
        Field field2 = ProductPopulated.intField("int").id(4L).build();
        Field field3 = ProductPopulated.dateField("date").id(5L).build();

        tableUtil.save(ProductPopulated.table()
                .id(2L)
                .productId(1L)
                .fields(newHashSet(field1, field2, field3))
                .build());

        migrateDatabase();

        Long nextFieldId = jdbcTemplate.queryForObject(NEXT_FIELD_ID_FROM_SEQ, Long.class);

        assertThat(nextFieldId).isEqualTo(6L);
    }

    @Test
    public void migrate_WithExistingPipelineSteps_SetsNextPipelineStepId() {
        PipelineStep step1 = ProductPopulated.mapPipelineStep().id(101L).selects(emptySet()).build();
        PipelineStep step2 = ProductPopulated.mapPipelineStep().id(102L).selects(emptySet()).build();
        PipelineStep step3 = ProductPopulated.mapPipelineStep().id(103L).selects(emptySet()).build();

        pipelineUtil.insert(ProductPopulated.pipeline()
                .id(2L)
                .steps(newHashSet(step1, step2, step3))
                .build());

        migrateDatabase();

        Long nextStepId = jdbcTemplate.queryForObject(NEXT_STEP_ID_FROM_SEQ, Long.class);

        assertThat(nextStepId).isEqualTo(104L);
    }

    @Test
    public void migrate_WithExistingPipelineStepSelects_SetsNextPipelineStepSelectId() {
        Select select1 = ProductPopulated.select().id(110L).select("a").build();
        Select select2 = ProductPopulated.select().id(111L).select("b").build();
        Select select3 = ProductPopulated.select().id(112L).select("c").build();

        PipelineStep step1 = ProductPopulated.mapPipelineStep()
                .id(101L)
                .selects(newHashSet(select1, select2, select3))
                .build();

        pipelineUtil.insert(ProductPopulated.pipeline()
                .id(2L)
                .steps(singleton(step1))
                .build());

        migrateDatabase();

        Long nextSelectId = jdbcTemplate.queryForObject(NEXT_SELECT_ID_FROM_SEQ, Long.class);

        assertThat(nextSelectId).isEqualTo(113L);
    }
}