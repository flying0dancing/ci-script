package com.lombardrisk.ignis.server.dataset.result;

import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.data.common.search.CombinedFilter;
import com.lombardrisk.ignis.data.common.search.Filter;
import com.lombardrisk.ignis.data.common.search.FilterExpression;
import com.lombardrisk.ignis.data.common.search.FilterOption;
import com.lombardrisk.ignis.server.dataset.config.DatasetTest;
import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import com.lombardrisk.ignis.server.dataset.fixture.JdbcFixture;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.pipeline.PipelineJpaRepository;
import com.lombardrisk.ignis.server.product.pipeline.PipelineService;
import com.lombardrisk.ignis.server.product.pipeline.PipelineStepService;
import com.lombardrisk.ignis.server.product.pipeline.SelectJpaRepository;
import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineAggregationStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineMapStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineWindowStep;
import com.lombardrisk.ignis.server.product.pipeline.model.TransformationType;
import com.lombardrisk.ignis.server.product.pipeline.select.PipelineFilter;
import com.lombardrisk.ignis.server.product.pipeline.select.Select;
import com.lombardrisk.ignis.server.product.pipeline.select.Window;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigRepository;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import com.lombardrisk.ignis.server.product.table.TableRepository;
import com.lombardrisk.ignis.server.product.table.model.Table;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.pipeline.step.api.DatasetFieldName.ROW_KEY;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

@RunWith(SpringRunner.class)
@DatasetTest
public class DatasetResultRepositoryIT {

    @Rule
    public final JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Autowired
    private JdbcTemplate phoenixJdbcTemplate;

    @Autowired
    private SelectJpaRepository selectJpaRepository;

    @Autowired
    private ProductConfigRepository productConfigRepository;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private PipelineJpaRepository pipelineJpaRepository;

    @Autowired
    private EntityManager entityManager;

    private JdbcFixture jdbcFixture;
    private DatasetResultRepository datasetResultRepository;
    private PipelineService pipelineService;

    private ProductConfig productConfig;
    private Table inputTable;
    private Table outputTable;
    private Pipeline pipeline;

    @Before
    public void setUp() {
        FilterToSQLConverter filterToSQLConverter = new FilterToSQLConverter(new LocalSQLFunctions());

        jdbcFixture = new JdbcFixture(phoenixJdbcTemplate);

        pipelineService = new PipelineService(pipelineJpaRepository);
        PipelineStepService pipelineStepService = new PipelineStepService(selectJpaRepository);

        datasetResultRepository = new DatasetResultRepository(
                phoenixJdbcTemplate, filterToSQLConverter, pipelineStepService);

        productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());

        inputTable = tableRepository.save(ProductPopulated.table()
                .productId(productConfig.getId())
                .physicalTableName("ATHLETES")
                .displayName("Athletes")
                .version(1)
                .build());

        outputTable = tableRepository.save(ProductPopulated.table()
                .productId(productConfig.getId())
                .physicalTableName("ATHLETES_RANKED")
                .displayName("Athletes Ranked")
                .version(1)
                .build());
    }

    private final List<JdbcFixture.ColumnDefinition> fellowshipHeaders = Arrays.asList(
            new JdbcFixture.ColumnDefinition(ROW_KEY.name(), "INTEGER"),
            new JdbcFixture.ColumnDefinition("NAME", "VARCHAR(255)"),
            new JdbcFixture.ColumnDefinition("RACE", "VARCHAR(255)"),
            new JdbcFixture.ColumnDefinition("FCR_SYS__SCHEMA_IN_PHYSICAL__ROW_KEY", "INTEGER")
    );

    @SuppressWarnings("all")
    @Test
    public void findOutputDatasetRowData_MapStep_ReturnsOnlyDatasetDataAndPaginatesResponse() {
        jdbcFixture.insertData("FELLOWSHIP", fellowshipHeaders, Arrays.asList(
                Arrays.asList(1, "MERIADOCK", "HOBBIT", 991),
                Arrays.asList(2, "SAM", "HOBBIT", 992),
                Arrays.asList(3, "FRODO", "HOBBIT", 993),
                Arrays.asList(4, "PEREGUIN", "HOBBIT", 994),
                Arrays.asList(5, "GIMLI", "DWARF", 995),
                Arrays.asList(6, "LEGOLAS", "ELF", 996)));

        Dataset fellowship = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .physicalTableName("FELLOWSHIP")
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.stringField("NAME").build(),
                                ProductPopulated.stringField("RACE").build())))
                        .build())
                .predicate("RACE = 'HOBBIT'")
                .build();

        PipelineMapStep drillBackStep = ProductPopulated.mapPipelineStep()
                .filters(newHashSet("CLUB = 'FELLOWSHIP'"))
                .build();

        DatasetRowData datasetResultsFirstPage = datasetResultRepository.findDrillbackOutputDatasetRowData(
                fellowship, drillBackStep, PageRequest.of(0, 2), null);

        soft.assertThat(datasetResultsFirstPage.getResultData().getContent())
                .containsExactlyInAnyOrder(
                        ImmutableMap.of(
                                "ROW_KEY", 1, "NAME", "MERIADOCK", "RACE", "HOBBIT",
                                "FCR_SYS__SCHEMA_IN_PHYSICAL__ROW_KEY", 991),
                        ImmutableMap.of(
                                "ROW_KEY", 2, "NAME", "SAM", "RACE", "HOBBIT",
                                "FCR_SYS__SCHEMA_IN_PHYSICAL__ROW_KEY", 992));
        soft.assertThat(datasetResultsFirstPage.getResultData().getNumber())
                .isEqualTo(0);
        soft.assertThat(datasetResultsFirstPage.getResultData().getTotalPages())
                .isEqualTo(2);
        soft.assertThat(datasetResultsFirstPage.getResultData().getNumberOfElements())
                .isEqualTo(2);

        DatasetRowData datasetResultsSecondPage =
                datasetResultRepository.findDrillbackInputDatasetRowData(
                        fellowship,
                        ProductPopulated.mapPipelineStep().filters(emptySet()).build(),
                        PageRequest.of(1, 2),
                        false,
                        null,
                        null);

        soft.assertThat(datasetResultsSecondPage.getResultData().getContent())
                .containsExactlyInAnyOrder(
                        ImmutableMap.of("ROW_KEY", 3, "NAME", "FRODO", "RACE", "HOBBIT"),
                        ImmutableMap.of("ROW_KEY", 4, "NAME", "PEREGUIN", "RACE", "HOBBIT"));
        soft.assertThat(datasetResultsSecondPage.getResultData().getNumber())
                .isEqualTo(1);
        soft.assertThat(datasetResultsSecondPage.getResultData().getTotalPages())
                .isEqualTo(2);
        soft.assertThat(datasetResultsSecondPage.getResultData().getNumberOfElements())
                .isEqualTo(2);
    }

    private final List<JdbcFixture.ColumnDefinition> characterHeaders = Arrays.asList(
            new JdbcFixture.ColumnDefinition(ROW_KEY.name(), "INTEGER"),
            new JdbcFixture.ColumnDefinition("NAME", "VARCHAR(255)"),
            new JdbcFixture.ColumnDefinition("RACE", "VARCHAR(255)"),
            new JdbcFixture.ColumnDefinition("CLUB", "VARCHAR(255)")
    );

    @SuppressWarnings("all")
    @Test
    public void findDrillbackInputDatasetRowData_PipelineMapStepWithFilters_ReturnsOnlyDatasetDataAndFilterValues() {
        jdbcFixture.insertData("CHARACTERS", characterHeaders, Arrays.asList(
                Arrays.asList(1, "ELROND", "ELF", "RIVENDELL"),
                Arrays.asList(2, "SAM", "HOBBIT", "FELLOWSHIP"),
                Arrays.asList(3, "FRODO", "HOBBIT", "FELLOWSHIP"),
                Arrays.asList(4, "PEREGUIN", "HOBBIT", "FELLOWSHIP"),
                Arrays.asList(5, "GIMLI", "DWARF", "FELLOWSHIP"),
                Arrays.asList(6, "LEGOLAS", "ELF", "FELLOWSHIP")));

        Dataset fellowship = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .physicalTableName("CHARACTERS")
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.stringField("NAME").build(),
                                ProductPopulated.stringField("RACE").build(),
                                ProductPopulated.stringField("CLUB").build())))
                        .build())
                .predicate("'I WILL TAKE IT' = 'I WILL TAKE IT'")
                .build();

        PipelineMapStep drillBackStep = ProductPopulated.mapPipelineStep()
                .filters(newHashSet("CLUB = 'FELLOWSHIP'"))
                .build();

        DatasetRowData datasetResultsFirstPage = datasetResultRepository.findDrillbackInputDatasetRowData(
                fellowship, drillBackStep, PageRequest.of(0, 2), false, null, null);

        soft.assertThat(datasetResultsFirstPage.getResultData().getContent())
                .containsExactlyInAnyOrder(
                        ImmutableMap.of(
                                "ROW_KEY", 1,
                                "NAME", "ELROND",
                                "RACE", "ELF",
                                "CLUB", "RIVENDELL",
                                "FCR_SYS__FILTERED", true),
                        ImmutableMap.of(
                                "ROW_KEY", 2,
                                "NAME", "SAM",
                                "RACE", "HOBBIT",
                                "CLUB", "FELLOWSHIP",
                                "FCR_SYS__FILTERED", false));
        soft.assertThat(datasetResultsFirstPage.getResultData().getNumber())
                .isEqualTo(0);
        soft.assertThat(datasetResultsFirstPage.getResultData().getTotalPages())
                .isEqualTo(3);
        soft.assertThat(datasetResultsFirstPage.getResultData().getNumberOfElements())
                .isEqualTo(2);
    }

    private final List<JdbcFixture.ColumnDefinition> quotesHeaders = Arrays.asList(
            new JdbcFixture.ColumnDefinition(ROW_KEY.name(), "INTEGER"),
            new JdbcFixture.ColumnDefinition("QUOTE", "VARCHAR(255)"),
            new JdbcFixture.ColumnDefinition("SPEAKER", "VARCHAR(255)"),
            new JdbcFixture.ColumnDefinition("FCR_SYS__SPEAKERS__ROW_KEY", "BIGINT"),
            new JdbcFixture.ColumnDefinition("FCR_SYS__QUOTES__ROW_KEY", "BIGINT")
    );

    @SuppressWarnings("all")
    @Test
    public void findDrillbackOutputDatasetRowData_JoinPipelineStep_AddsExtraColumnToResultSet() {
        jdbcFixture.insertData("QUOTES_AND_SPEAKERS", quotesHeaders, Arrays.asList(
                Arrays.asList(1, "Breathe the free air Theoden King", "Gandalf", 900, 1000),
                Arrays.asList(2, "The love of the halflings leaf has slowed your mind", "Saruman", 901, 1001),
                Arrays.asList(3, "It is a gift", "Boromir", 902, 1002),
                Arrays.asList(4, "Isildurrrrrr!", "Elrond", 903, 1003),
                Arrays.asList(5, "And I suppose you think youre the one to do it!", "Gimili", 904, 1004)));

        Dataset fellowship = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .physicalTableName("QUOTES_AND_SPEAKERS")
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.stringField("QUOTE").build(),
                                ProductPopulated.stringField("SPEAKER").build())))
                        .build())
                .recordsCount(5L)
                .predicate("1=1")
                .build();

        DatasetRowData datasetResultsFirstPage =
                datasetResultRepository.findDrillbackOutputDatasetRowData(
                        fellowship,
                        ProductPopulated.joinPipelineStep()
                                .schemaOut(ProductPopulated.schemaDetails().build())
                                .joins(newHashSet(
                                        ProductPopulated.join()
                                                .leftSchema(ProductPopulated.schemaDetails()
                                                        .physicalTableName("QUOTES")
                                                        .build())
                                                .rightSchema(ProductPopulated.schemaDetails()
                                                        .physicalTableName("SPEAKERS")
                                                        .build())
                                                .build()))
                                .build(),
                        PageRequest.of(0, 2), null);

        soft.assertThat(datasetResultsFirstPage.getResultData().getContent())
                .containsExactlyInAnyOrder(
                        ImmutableMap.of(
                                "ROW_KEY", 1,
                                "QUOTE", "Breathe the free air Theoden King",
                                "SPEAKER", "Gandalf",
                                "FCR_SYS__SPEAKERS__ROW_KEY", 900L,
                                "FCR_SYS__QUOTES__ROW_KEY", 1000L),
                        ImmutableMap.of(
                                "ROW_KEY", 2,
                                "QUOTE", "The love of the halflings leaf has slowed your mind",
                                "SPEAKER", "Saruman",
                                "FCR_SYS__SPEAKERS__ROW_KEY", 901L,
                                "FCR_SYS__QUOTES__ROW_KEY", 1001L)
                );

        soft.assertThat(datasetResultsFirstPage.getResultData().getNumber())
                .isEqualTo(0);
        soft.assertThat(datasetResultsFirstPage.getResultData().getTotalPages())
                .isEqualTo(3);
        soft.assertThat(datasetResultsFirstPage.getResultData().getNumberOfElements())
                .isEqualTo(2);
    }

    private final List<JdbcFixture.ColumnDefinition> lansraadHouses = Arrays.asList(
            new JdbcFixture.ColumnDefinition(ROW_KEY.name(), "INTEGER"),
            new JdbcFixture.ColumnDefinition("MAJOR_HOUSE", "VARCHAR(255)"),
            new JdbcFixture.ColumnDefinition("WEALTH_PER_SUBJECT", "NUMBER(10,2)"),
            new JdbcFixture.ColumnDefinition("FCR_SYS__IMPERIAL_SUBJECTS__MAJOR_HOUSE", "VARCHAR(255)")
    );

    @SuppressWarnings("all")
    @Test
    public void findDrillbackOutputDatasetRowData_AggregationPipelineStep_AddsExtraColumnToResultSet() {
        jdbcFixture.insertData("LANSRAAD_HOUSES", lansraadHouses, Arrays.asList(
                Arrays.asList(1, "Atreides", 12.92, "Atreides"),
                Arrays.asList(2, "Harkonnen", 9.31, "Harkonnen"),
                Arrays.asList(3, "Corinno", 14.34, "Corinno"),
                Arrays.asList(4, "Ecaz", 10.11, "Ecaz")));

        Dataset fellowship = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .physicalTableName("LANSRAAD_HOUSES")
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.stringField("MAJOR_HOUSE").build(),
                                ProductPopulated.decimalField("WEALTH_PER_SUBJECT").build())))
                        .build())
                .recordsCount(5L)
                .predicate("1=1")
                .build();

        DatasetRowData datasetResultsFirstPage =
                datasetResultRepository.findDrillbackOutputDatasetRowData(
                        fellowship,
                        ProductPopulated.aggregatePipelineStep()
                                .schemaIn(ProductPopulated.schemaDetails()
                                        .physicalTableName("IMPERIAL_SUBJECTS")
                                        .build())
                                .schemaOut(ProductPopulated.schemaDetails()
                                        .physicalTableName("LANSRAAD_HOUSES")
                                        .build())
                                .groupings(newHashSet("MAJOR_HOUSE"))
                                .filters(emptySet())
                                .build(),
                        PageRequest.of(0, 2), null);

        soft.assertThat(datasetResultsFirstPage.getResultData().getContent())
                .isEqualTo(Arrays.asList(
                        ImmutableMap.of(
                                "ROW_KEY", 1,
                                "MAJOR_HOUSE", "Atreides",
                                "WEALTH_PER_SUBJECT", BigDecimal.valueOf(12.92),
                                "FCR_SYS__IMPERIAL_SUBJECTS__MAJOR_HOUSE", "Atreides"),
                        ImmutableMap.of(
                                "ROW_KEY", 2,
                                "MAJOR_HOUSE", "Harkonnen",
                                "WEALTH_PER_SUBJECT", BigDecimal.valueOf(9.31),
                                "FCR_SYS__IMPERIAL_SUBJECTS__MAJOR_HOUSE", "Harkonnen")));

        soft.assertThat(datasetResultsFirstPage.getResultData().getNumber())
                .isEqualTo(0);
        soft.assertThat(datasetResultsFirstPage.getResultData().getTotalPages())
                .isEqualTo(2);
        soft.assertThat(datasetResultsFirstPage.getResultData().getNumberOfElements())
                .isEqualTo(2);
    }

    private final List<JdbcFixture.ColumnDefinition> currencyRank = Arrays.asList(
            new JdbcFixture.ColumnDefinition(ROW_KEY.name(), "INTEGER"),
            new JdbcFixture.ColumnDefinition("CURRENCY", "VARCHAR(255)"),
            new JdbcFixture.ColumnDefinition("RANK", "INTEGER")
    );

    @Test
    public void findDrillbackInputDatasetRowData_AggregationWithFilters_AddsExtraWasExcludedColumn() {
        jdbcFixture.insertData("CURRENCY_RANK", currencyRank, Arrays.asList(
                Arrays.asList(1, "GBP", 1),
                Arrays.asList(2, "USD", 2),
                Arrays.asList(3, "AUD", 9),
                Arrays.asList(4, "EUR", 9),
                Arrays.asList(5, "JPY", 9)));

        Dataset fellowship = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .physicalTableName("CURRENCY_RANK")
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.stringField("CURRENCY").build(),
                                ProductPopulated.intField("RANK").build())))
                        .build())
                .recordsCount(5L)
                .predicate("1=1")
                .build();

        DatasetRowData datasetResultsFirstPage =
                datasetResultRepository.findDrillbackInputDatasetRowData(
                        fellowship,
                        ProductPopulated.aggregatePipelineStep()
                                .schemaIn(ProductPopulated.schemaDetails().physicalTableName("CURRENCY_RANK").build())
                                .schemaOut(ProductPopulated.schemaDetails()
                                        .physicalTableName("LEAST_TRADED_CURRENCIES")
                                        .build())
                                .groupings(emptySet())
                                .filters(newHashSet("RANK = 9"))
                                .build(),
                        PageRequest.of(0, 5),
                        false, null, null);

        soft.assertThat(datasetResultsFirstPage.getResultData().getContent())
                .isEqualTo(Arrays.asList(
                        ImmutableMap.of(
                                "ROW_KEY", 1, "CURRENCY", "GBP", "RANK", 1, "FCR_SYS__FILTERED", true),
                        ImmutableMap.of(
                                "ROW_KEY", 2, "CURRENCY", "USD", "RANK", 2, "FCR_SYS__FILTERED", true),
                        ImmutableMap.of(
                                "ROW_KEY", 3, "CURRENCY", "AUD", "RANK", 9, "FCR_SYS__FILTERED", false),
                        ImmutableMap.of(
                                "ROW_KEY", 4, "CURRENCY", "EUR", "RANK", 9, "FCR_SYS__FILTERED", false),
                        ImmutableMap.of(
                                "ROW_KEY", 5, "CURRENCY", "JPY", "RANK", 9, "FCR_SYS__FILTERED", false)));

        soft.assertThat(datasetResultsFirstPage.getResultData().getNumber())
                .isEqualTo(0);
        soft.assertThat(datasetResultsFirstPage.getResultData().getTotalPages())
                .isEqualTo(1);
        soft.assertThat(datasetResultsFirstPage.getResultData().getNumberOfElements())
                .isEqualTo(5);
    }

    @Test
    public void findDrillbackInputDatasetRowData_AggregationWithMultipleGroupingsAndDrillbackRowsOnly_ReturnsDrillbackRows() {
        List<JdbcFixture.ColumnDefinition> inputSchema = Arrays.asList(
                new JdbcFixture.ColumnDefinition(ROW_KEY.name(), "INTEGER"),
                new JdbcFixture.ColumnDefinition("Name", "VARCHAR(255)"),
                new JdbcFixture.ColumnDefinition("Country", "VARCHAR(255)"));

        List<JdbcFixture.ColumnDefinition> outputSchema = Arrays.asList(
                new JdbcFixture.ColumnDefinition(ROW_KEY.name(), "INTEGER"),
                new JdbcFixture.ColumnDefinition("Name", "VARCHAR(255)"),
                new JdbcFixture.ColumnDefinition("Country", "VARCHAR(255)"),
                new JdbcFixture.ColumnDefinition("Count", "INTEGER"),
                new JdbcFixture.ColumnDefinition("FCR_SYS__NAMES__Name", "VARCHAR(255)"),
                new JdbcFixture.ColumnDefinition("FCR_SYS__NAMES__Country", "VARCHAR(255)"));

        jdbcFixture.insertData("NAMES", inputSchema, Arrays.asList(
                Arrays.asList(1, "Thomas Anderson", "Cameroon"),
                Arrays.asList(2, "Thomas Anderson", "Montenegro"),
                Arrays.asList(3, "Thomas Anderson", "Montenegro"),
                Arrays.asList(4, "Thomas Anderson", "Switzerland"),
                Arrays.asList(5, "Thomas Anderson", "Switzerland"),
                Arrays.asList(6, "Thomas Anderson", "Switzerland"),
                Arrays.asList(7, "Fred Smith", "Switzerland")));

        jdbcFixture.insertData("NAMES_GROUPED", outputSchema, Arrays.asList(
                Arrays.asList(10001, "Thomas Anderson", "Cameroon", 1, "Thomas Anderson", "Cameroon"),
                Arrays.asList(10002, "Thomas Anderson", "Montenegro", 2, "Thomas Anderson", "Montenegro"),
                Arrays.asList(10003, "Thomas Anderson", "Switzerland", 3, "Thomas Anderson", "Switzerland"),
                Arrays.asList(10004, "Fred Smith", "Switzerland", 1, "Fred Smith", "Switzerland")));

        Dataset namesDataset = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .physicalTableName("NAMES")
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.stringField("Name").build(),
                                ProductPopulated.stringField("Country").build())))
                        .build())
                .predicate("1=1")
                .build();

        PipelineAggregationStep aggregationStep = ProductPopulated.aggregatePipelineStep()
                .schemaIn(ProductPopulated.schemaDetails().physicalTableName("NAMES").build())
                .schemaOut(ProductPopulated.schemaDetails().physicalTableName("NAMES_GROUPED").build())
                .groupings(newHashSet("Name", "Country"))
                .build();

        DatasetRowData inputDatasetRowData = datasetResultRepository.findDrillbackInputDatasetRowData(
                namesDataset, aggregationStep, PageRequest.of(0, 7), true, 10002L, null);

        soft.assertThat(inputDatasetRowData.getResultData().getContent())
                .isEqualTo(Arrays.asList(
                        ImmutableMap.of("ROW_KEY", 2, "Name", "Thomas Anderson", "Country", "Montenegro"),
                        ImmutableMap.of("ROW_KEY", 3, "Name", "Thomas Anderson", "Country", "Montenegro")));
    }

    @Test
    public void findDrillbackInputDatasetRowData_AggregationWithNoGroupingsAndDrillbackRowsOnly_ReturnsDrillbackRows() {
        List<JdbcFixture.ColumnDefinition> inputSchema = Arrays.asList(
                new JdbcFixture.ColumnDefinition(ROW_KEY.name(), "INTEGER"),
                new JdbcFixture.ColumnDefinition("Name", "VARCHAR(255)"),
                new JdbcFixture.ColumnDefinition("Country", "VARCHAR(255)"));

        List<JdbcFixture.ColumnDefinition> outputSchema = Arrays.asList(
                new JdbcFixture.ColumnDefinition(ROW_KEY.name(), "INTEGER"),
                new JdbcFixture.ColumnDefinition("Count", "INTEGER"));

        jdbcFixture.insertData("NAMES", inputSchema, Arrays.asList(
                Arrays.asList(1, "Thomas Anderson", "Cameroon"),
                Arrays.asList(2, "Thomas Anderson", "Montenegro"),
                Arrays.asList(3, "Thomas Anderson", "Montenegro"),
                Arrays.asList(4, "Thomas Anderson", "Switzerland"),
                Arrays.asList(5, "Thomas Anderson", "Switzerland"),
                Arrays.asList(6, "Thomas Anderson", "Switzerland"),
                Arrays.asList(7, "Fred Smith", "Switzerland")));

        jdbcFixture.insertData("TOTAL_NAMES", outputSchema, Collections.singletonList(Arrays.asList(10001, 7)));

        Dataset namesDataset = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .physicalTableName("NAMES")
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.stringField("Name").build(),
                                ProductPopulated.stringField("Country").build())))
                        .build())
                .predicate("1=1")
                .build();

        PipelineAggregationStep aggregationStep = ProductPopulated.aggregatePipelineStep()
                .schemaIn(ProductPopulated.schemaDetails().physicalTableName("NAMES").build())
                .schemaOut(ProductPopulated.schemaDetails().physicalTableName("TOTAL_NAMES").build())
                .groupings(emptySet())
                .build();

        DatasetRowData inputDatasetRowData = datasetResultRepository.findDrillbackInputDatasetRowData(
                namesDataset, aggregationStep, PageRequest.of(0, 7), true, 10001L, null);

        soft.assertThat(inputDatasetRowData.getResultData().getContent())
                .isEqualTo(Arrays.asList(
                        ImmutableMap.of("ROW_KEY", 1, "Name", "Thomas Anderson", "Country", "Cameroon"),
                        ImmutableMap.of("ROW_KEY", 2, "Name", "Thomas Anderson", "Country", "Montenegro"),
                        ImmutableMap.of("ROW_KEY", 3, "Name", "Thomas Anderson", "Country", "Montenegro"),
                        ImmutableMap.of("ROW_KEY", 4, "Name", "Thomas Anderson", "Country", "Switzerland"),
                        ImmutableMap.of("ROW_KEY", 5, "Name", "Thomas Anderson", "Country", "Switzerland"),
                        ImmutableMap.of("ROW_KEY", 6, "Name", "Thomas Anderson", "Country", "Switzerland"),
                        ImmutableMap.of("ROW_KEY", 7, "Name", "Fred Smith", "Country", "Switzerland")));
    }

    @Test
    public void findDrillbackInputDatasetRowData_BothDrillbackRowsOnlyAndSearchFilterApplied_ReturnsFilteredDrillbackRows() {
        List<JdbcFixture.ColumnDefinition> inputSchema = Arrays.asList(
                new JdbcFixture.ColumnDefinition(ROW_KEY.name(), "INTEGER"),
                new JdbcFixture.ColumnDefinition("Name", "VARCHAR(255)"),
                new JdbcFixture.ColumnDefinition("Address", "VARCHAR(255)"),
                new JdbcFixture.ColumnDefinition("Country", "VARCHAR(255)"));

        jdbcFixture.insertData("NAMES", inputSchema, Arrays.asList(
                Arrays.asList(1, "Thomas Anderson", "123 TestThis Street", "Cameroon"),
                Arrays.asList(2, "Thomas Anderson", "124 TestThis Street", "Montenegro"),
                Arrays.asList(3, "Thomas Anderson", "125 TestThis Street", "Montenegro"),
                Arrays.asList(4, "Thomas Anderson", "123 TestThis Street", "Switzerland"),
                Arrays.asList(5, "Thomas Anderson", "123 TestThis Road", "Switzerland"),
                Arrays.asList(6, "Thomas Anderson", "456 TestThis Street", "Switzerland"),
                Arrays.asList(7, "Fred Smith", "123 TestThis Street", "Switzerland")));

        List<JdbcFixture.ColumnDefinition> outputSchema = Arrays.asList(
                new JdbcFixture.ColumnDefinition(ROW_KEY.name(), "INTEGER"),
                new JdbcFixture.ColumnDefinition("Name", "VARCHAR(255)"),
                new JdbcFixture.ColumnDefinition("Country", "VARCHAR(255)"),
                new JdbcFixture.ColumnDefinition("Count", "INTEGER"),
                new JdbcFixture.ColumnDefinition("FCR_SYS__NAMES__Name", "VARCHAR(255)"),
                new JdbcFixture.ColumnDefinition("FCR_SYS__NAMES__Country", "VARCHAR(255)"));

        jdbcFixture.insertData("NAMES_GROUPED", outputSchema, Arrays.asList(
                Arrays.asList(10001, "Thomas Anderson", "Cameroon", 1, "Thomas Anderson", "Cameroon"),
                Arrays.asList(10002, "Thomas Anderson", "Montenegro", 2, "Thomas Anderson", "Montenegro"),
                Arrays.asList(10003, "Thomas Anderson", "Switzerland", 3, "Thomas Anderson", "Switzerland"),
                Arrays.asList(10004, "Fred Smith", "Switzerland", 1, "Fred Smith", "Switzerland")));

        Dataset namesDataset = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .physicalTableName("NAMES")
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.stringField("Name").build(),
                                ProductPopulated.stringField("Address").build(),
                                ProductPopulated.stringField("Country").build())))
                        .build())
                .predicate("1=1")
                .build();

        PipelineAggregationStep aggregationStep = ProductPopulated.aggregatePipelineStep()
                .schemaIn(ProductPopulated.schemaDetails().physicalTableName("NAMES").build())
                .schemaOut(ProductPopulated.schemaDetails().physicalTableName("NAMES_GROUPED").build())
                .groupings(newHashSet("Name", "Country"))
                .build();

        FilterExpression addressStartingWith123 = Filter.builder()
                .columnName("Address").type(FilterOption.STARTS_WITH).filter("123").build();
        FilterExpression addressEndingInStreet = Filter.builder()
                .columnName("Address").type(FilterOption.ENDS_WITH).filter(" Street").build();

        DatasetRowData addressesStartingWith123 = datasetResultRepository.findDrillbackInputDatasetRowData(
                namesDataset, aggregationStep, PageRequest.of(0, 7), true, 10003L, addressStartingWith123);

        soft.assertThat(addressesStartingWith123.getResultData().getContent())
                .isEqualTo(Arrays.asList(
                        ImmutableMap.of("ROW_KEY", 4,
                                "Name", "Thomas Anderson", "Address", "123 TestThis Street", "Country", "Switzerland"),
                        ImmutableMap.of("ROW_KEY", 5,
                                "Name", "Thomas Anderson", "Address", "123 TestThis Road", "Country", "Switzerland")));

        DatasetRowData addressesEndingInStreet = datasetResultRepository.findDrillbackInputDatasetRowData(
                namesDataset, aggregationStep, PageRequest.of(0, 7), true, 10003L, addressEndingInStreet);

        soft.assertThat(addressesEndingInStreet.getResultData().getContent())
                .isEqualTo(Arrays.asList(
                        ImmutableMap.of("ROW_KEY", 4,
                                "Name", "Thomas Anderson", "Address", "123 TestThis Street", "Country", "Switzerland"),
                        ImmutableMap.of("ROW_KEY", 6,
                                "Name", "Thomas Anderson", "Address", "456 TestThis Street", "Country", "Switzerland"))
                );
    }

    @Test
    public void findDrillbackInputDatasetRowData_AggregationStepGroupingByDateAndDrillbackRowsOnly_ReturnsDrillbackRows() {
        List<JdbcFixture.ColumnDefinition> inputSchema = Arrays.asList(
                new JdbcFixture.ColumnDefinition(ROW_KEY.name(), "INTEGER"),
                new JdbcFixture.ColumnDefinition("Name", "VARCHAR(255)"),
                new JdbcFixture.ColumnDefinition("Birthday", "DATE"));

        LocalDate firstJan = LocalDate.of(1984, 1, 1);
        LocalDate secondJan = LocalDate.of(1984, 1, 2);
        LocalDate twentySecondNov = LocalDate.of(1984, 11, 22);
        LocalDate fifthJune = LocalDate.of(1984, 6, 5);

        jdbcFixture.insertData("BIRTHDAYS", inputSchema, Arrays.asList(
                Arrays.asList(1, "Vernell Heidecker", firstJan),
                Arrays.asList(2, "Collette Brandow", secondJan),
                Arrays.asList(3, "Alyce Lebow", firstJan),
                Arrays.asList(4, "Maudie Considine", firstJan),
                Arrays.asList(5, "Jesusa Pehrson", twentySecondNov),
                Arrays.asList(6, "Mariette Burkes", fifthJune),
                Arrays.asList(7, "Fatima Gailey", twentySecondNov)));

        List<JdbcFixture.ColumnDefinition> outputSchema = Arrays.asList(
                new JdbcFixture.ColumnDefinition(ROW_KEY.name(), "INTEGER"),
                new JdbcFixture.ColumnDefinition("Birthday", "DATE"),
                new JdbcFixture.ColumnDefinition("Count", "INTEGER"),
                new JdbcFixture.ColumnDefinition("FCR_SYS__BIRTHDAYS__Birthday", "DATE"));

        jdbcFixture.insertData("BIRTHDAYS_GROUPED", outputSchema, Arrays.asList(
                Arrays.asList(10001, firstJan, 3, firstJan),
                Arrays.asList(10002, secondJan, 1, secondJan),
                Arrays.asList(10003, twentySecondNov, 2, twentySecondNov),
                Arrays.asList(10004, fifthJune, 1, fifthJune)));

        Dataset namesDataset = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .physicalTableName("BIRTHDAYS")
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.stringField("Name").build(),
                                ProductPopulated.dateField("Birthday").build())))
                        .build())
                .predicate("1=1")
                .build();

        PipelineAggregationStep aggregationStep = ProductPopulated.aggregatePipelineStep()
                .schemaIn(ProductPopulated.schemaDetails().physicalTableName("BIRTHDAYS").build())
                .schemaOut(ProductPopulated.schemaDetails().physicalTableName("BIRTHDAYS_GROUPED").build())
                .groupings(newHashSet("Birthday"))
                .build();

        DatasetRowData inputDatasetRowData = datasetResultRepository.findDrillbackInputDatasetRowData(
                namesDataset, aggregationStep, PageRequest.of(0, 7), true, 10003L, null);

        soft.assertThat(inputDatasetRowData.getResultData().getContent())
                .isEqualTo(Arrays.asList(
                        ImmutableMap.of("ROW_KEY", 5, "Name", "Jesusa Pehrson", "Birthday", toDate(twentySecondNov)),
                        ImmutableMap.of("ROW_KEY", 7, "Name", "Fatima Gailey", "Birthday", toDate(twentySecondNov))));
    }

    @Test
    public void findDrillbackInputDatasetRowData_WindowStepWithMultiplePartitionsAndDrillbackRowsOnly_ReturnsDrillbackRows() {
        List<JdbcFixture.ColumnDefinition> inputSchema = Arrays.asList(
                new JdbcFixture.ColumnDefinition(ROW_KEY.name(), "INTEGER"),
                new JdbcFixture.ColumnDefinition("Name", "VARCHAR(255)"),
                new JdbcFixture.ColumnDefinition("Country", "VARCHAR(255)"),
                new JdbcFixture.ColumnDefinition("Event", "VARCHAR(255)"),
                new JdbcFixture.ColumnDefinition("Score", "INTEGER"));

        List<JdbcFixture.ColumnDefinition> outputSchema = Arrays.asList(
                new JdbcFixture.ColumnDefinition(ROW_KEY.name(), "INTEGER"),
                new JdbcFixture.ColumnDefinition("Name", "VARCHAR(255)"),
                new JdbcFixture.ColumnDefinition("Country", "VARCHAR(255)"),
                new JdbcFixture.ColumnDefinition("Event", "VARCHAR(255)"),
                new JdbcFixture.ColumnDefinition("Rank", "INTEGER"),
                new JdbcFixture.ColumnDefinition("FCR_SYS__ATHLETES__Country", "VARCHAR(255)"),
                new JdbcFixture.ColumnDefinition("FCR_SYS__ATHLETES__Event", "VARCHAR(255)"));

        jdbcFixture.insertData("ATHLETES", inputSchema, Arrays.asList(
                Arrays.asList(1, "Vernell Heidecker", "Afghanistan", "100M", 7),
                Arrays.asList(2, "Collette Brandow", "Afghanistan", "Hurdles", 3),
                Arrays.asList(3, "Alyce Lebow", "Afghanistan", "100M", 3),
                Arrays.asList(4, "Maudie Considine", "Azerbaijan", "Discus", 2),
                Arrays.asList(5, "Jesusa Pehrson", "Azerbaijan", "Discus", 3),
                Arrays.asList(6, "Mariette Burkes", "Brunei Darussalam", "Relay", 5),
                Arrays.asList(7, "Fatima Gailey", "Brunei Darussalam", "100M", 12)));

        jdbcFixture.insertData("ATHLETES_RANKED", outputSchema, Arrays.asList(
                Arrays.asList(111111, "Vernell Heidecker", "Afghanistan", "100M", 2, "Afghanistan", "100M"),
                Arrays.asList(222222, "Collette Brandow", "Afghanistan", "Hurdles", 1, "Afghanistan", "Hurdles"),
                Arrays.asList(333333, "Alyce Lebow", "Afghanistan", "100M", 1, "Afghanistan", "100M"),
                Arrays.asList(444444, "Maudie Considine", "Azerbaijan", "Discus", 1, "Azerbaijan", "Discus"),
                Arrays.asList(555555, "Jesusa Pehrson", "Azerbaijan", "Discus", 2, "Azerbaijan", "Discus"),
                Arrays.asList(666666, "Mariette Burkes", "Brunei Darussalam", "Relay", 1, "Brunei Darussalam", "Relay"),
                Arrays.asList(777777, "Fatima Gailey", "Brunei Darussalam", "100M", 1, "Brunei Darussalam", "100M")));

        Dataset namesDataset = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .physicalTableName("ATHLETES")
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.stringField("Name").build(),
                                ProductPopulated.stringField("Country").build(),
                                ProductPopulated.stringField("Event").build(),
                                ProductPopulated.intField("Score").build())))
                        .build())
                .predicate("1=1")
                .build();

        Select select = ProductPopulated.select()
                .select("rank()")
                .isWindow(true)
                .window(Window.builder().partitions(newHashSet("Country", "Event")).build())
                .build();

        PipelineWindowStep windowStep = ProductPopulated.windowPipelineStep()
                .schemaInId(inputTable.getId())
                .schemaIn(ProductPopulated.schemaDetails()
                        .id(inputTable.getId()).physicalTableName("ATHLETES").displayName("Athletes")
                        .build())
                .schemaOutId(outputTable.getId())
                .schemaOut(ProductPopulated.schemaDetails()
                        .id(outputTable.getId()).physicalTableName("ATHLETES_RANKED").displayName("Athletes Ranked")
                        .build())
                .selects(newHashSet(select))
                .build();

        Pipeline pipeline = ProductPopulated.pipeline()
                .productId(productConfig.getId())
                .steps(singleton(windowStep))
                .build();

        windowStep.setPipeline(pipeline);
        select.setPipelineStep(windowStep);

        pipelineJpaRepository.save(pipeline);

        entityManager.flush();
        entityManager.clear();

        DatasetRowData inputDatasetRowData = datasetResultRepository.findDrillbackInputDatasetRowData(
                namesDataset, windowStep, PageRequest.of(0, 7), true, 111111L, null);

        soft.assertThat(inputDatasetRowData.getResultData().getContent())
                .isEqualTo(Arrays.asList(
                        ImmutableMap.of("ROW_KEY", 1,
                                "Name", "Vernell Heidecker", "Country", "Afghanistan", "Event", "100M", "Score", 7),
                        ImmutableMap.of("ROW_KEY", 3,
                                "Name", "Alyce Lebow", "Country", "Afghanistan", "Event", "100M", "Score", 3)));
    }

    @Test
    public void findDrillbackInputDatasetRowData_WindowStepWithFiltersAndDrillbackRowsOnly_ReturnsDrillbackRows() {
        List<JdbcFixture.ColumnDefinition> inputSchema = Arrays.asList(
                new JdbcFixture.ColumnDefinition(ROW_KEY.name(), "INTEGER"),
                new JdbcFixture.ColumnDefinition("Name", "VARCHAR(255)"),
                new JdbcFixture.ColumnDefinition("Country", "VARCHAR(255)"),
                new JdbcFixture.ColumnDefinition("Event", "VARCHAR(255)"),
                new JdbcFixture.ColumnDefinition("Score", "INTEGER"));

        List<JdbcFixture.ColumnDefinition> outputSchema = Arrays.asList(
                new JdbcFixture.ColumnDefinition(ROW_KEY.name(), "INTEGER"),
                new JdbcFixture.ColumnDefinition("Name", "VARCHAR(255)"),
                new JdbcFixture.ColumnDefinition("Country", "VARCHAR(255)"),
                new JdbcFixture.ColumnDefinition("Event", "VARCHAR(255)"),
                new JdbcFixture.ColumnDefinition("Rank", "INTEGER"),
                new JdbcFixture.ColumnDefinition("FCR_SYS__ATHLETES__Event", "VARCHAR(255)"));

        jdbcFixture.insertData("ATHLETES", inputSchema, Arrays.asList(
                Arrays.asList(1, "Vernell Heidecker", "Afghanistan", "100M", 7),
                Arrays.asList(2, "Collette Brandow", "Afghanistan", "Hurdles", 3),
                Arrays.asList(3, "Alyce Lebow", "Afghanistan", "100M", 3),
                Arrays.asList(4, "Maudie Considine", "Azerbaijan", "Discus", 2),
                Arrays.asList(5, "Jesusa Pehrson", "Azerbaijan", "Discus", 3),
                Arrays.asList(6, "Mariette Burkes", "Brunei Darussalam", "Relay", 5),
                Arrays.asList(7, "Fatima Gailey", "Brunei Darussalam", "100M", 12)));

        jdbcFixture.insertData("ATHLETES_RANKED", outputSchema, Arrays.asList(
                Arrays.asList(111111, "Vernell Heidecker", "Afghanistan", "100M", 2, "100M"),
                Arrays.asList(222222, "Collette Brandow", "Afghanistan", "Hurdles", 1, "Hurdles"),
                Arrays.asList(333333, "Alyce Lebow", "Afghanistan", "100M", 1, "100M"),
                Arrays.asList(444444, "Maudie Considine", "Azerbaijan", "Discus", 1, "Discus"),
                Arrays.asList(555555, "Jesusa Pehrson", "Azerbaijan", "Discus", 2, "Discus"),
                Arrays.asList(666666, "Mariette Burkes", "Brunei Darussalam", "Relay", 1, "Relay"),
                Arrays.asList(777777, "Fatima Gailey", "Brunei Darussalam", "100M", 3, "100M")));

        Dataset namesDataset = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .physicalTableName("ATHLETES")
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.stringField("Name").build(),
                                ProductPopulated.stringField("Country").build(),
                                ProductPopulated.stringField("Event").build(),
                                ProductPopulated.intField("Score").build())))
                        .build())
                .predicate("1=1")
                .build();

        Pipeline pipeline = pipelineService.savePipeline(ProductPopulated.pipeline()
                .productId(productConfig.getId())
                .steps(singleton(ProductPopulated.windowPipelineStep()
                        .schemaInId(inputTable.getId())
                        .schemaIn(ProductPopulated.schemaDetails()
                                .id(inputTable.getId()).physicalTableName("ATHLETES").displayName("Athletes")
                                .build())
                        .schemaOutId(outputTable.getId())
                        .schemaOut(ProductPopulated.schemaDetails()
                                .id(outputTable.getId())
                                .physicalTableName("ATHLETES_RANKED")
                                .displayName("Athletes Ranked")
                                .build())
                        .selects(newHashSet(ProductPopulated.select()
                                .select("rank()")
                                .isWindow(true)
                                .window(Window.builder().partitions(newHashSet("Event")).build())
                                .build()))
                        .filters(newHashSet("Country = 'Brunei Darussalam'"))
                        .build()))
                .build());

        entityManager.flush();
        entityManager.clear();

        PipelineStep windowStep = pipeline.getSteps().iterator().next();

        DatasetRowData inputDatasetRowData = datasetResultRepository.findDrillbackInputDatasetRowData(
                namesDataset, windowStep, PageRequest.of(0, 7), true, 777777L, null);

        soft.assertThat(inputDatasetRowData.getResultData().getContent())
                .isEqualTo(Collections.singletonList(ImmutableMap.builder()
                        .put("ROW_KEY", 7)
                        .put("FCR_SYS__FILTERED", false)
                        .put("Name", "Fatima Gailey")
                        .put("Country", "Brunei Darussalam")
                        .put("Event", "100M")
                        .put("Score", 12)
                        .build()));
    }

    @Test
    public void findOutputDatasetRowData_MapStepWithSort_ReturnsOnlyDatasetDataAndPaginatesResponse() {
        jdbcFixture.insertData("FELLOWSHIP", fellowshipHeaders, Arrays.asList(
                Arrays.asList(1, "MERIADOCK", "HOBBIT", 991),
                Arrays.asList(2, "PEREGUIN", "HOBBIT", 992),
                Arrays.asList(3, "SIMLI", "DWARF", 993),
                Arrays.asList(4, "SAM", "HOBBIT", 994),
                Arrays.asList(5, "MRODO", "HOBBIT", 995),
                Arrays.asList(6, "LEGOLAS", "ELF", 996)));

        Dataset fellowship = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .physicalTableName("FELLOWSHIP")
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.stringField("NAME").build(),
                                ProductPopulated.stringField("RACE").build())))
                        .build())
                .predicate("RACE = 'HOBBIT'")
                .build();

        PipelineMapStep drillBackStep = ProductPopulated.mapPipelineStep()
                .filters(newHashSet("CLUB = 'FELLOWSHIP'"))
                .build();

        DatasetRowData datasetResultsFirstPage = datasetResultRepository.findDrillbackOutputDatasetRowData(
                fellowship, drillBackStep, PageRequest.of(0, 2,
                        Sort.by(
                                Sort.Order.by("NAME")
                                        .with(Sort.Direction.DESC),
                                Sort.Order.by("RACE")
                                        .with(Sort.Direction.ASC))), null);

        soft.assertThat(datasetResultsFirstPage.getResultData().getContent())
                .containsExactlyInAnyOrder(
                        ImmutableMap.of(
                                "ROW_KEY", 2, "NAME", "PEREGUIN", "RACE", "HOBBIT",
                                "FCR_SYS__SCHEMA_IN_PHYSICAL__ROW_KEY", 992),
                        ImmutableMap.of(
                                "ROW_KEY", 4, "NAME", "SAM", "RACE", "HOBBIT",
                                "FCR_SYS__SCHEMA_IN_PHYSICAL__ROW_KEY", 994));
        soft.assertThat(datasetResultsFirstPage.getResultData().getNumber())
                .isEqualTo(0);
        soft.assertThat(datasetResultsFirstPage.getResultData().getTotalPages())
                .isEqualTo(2);
        soft.assertThat(datasetResultsFirstPage.getResultData().getNumberOfElements())
                .isEqualTo(2);

        DatasetRowData datasetResultsSecondPage =
                datasetResultRepository.findDrillbackInputDatasetRowData(
                        fellowship, ProductPopulated.mapPipelineStep().filters(emptySet()).build(), PageRequest.of(1, 2,
                                Sort.by(
                                        Sort.Order.by("NAME")
                                                .with(Sort.Direction.DESC),
                                        Sort.Order.by("RACE")
                                                .with(Sort.Direction.ASC))), false, null, null);

        soft.assertThat(datasetResultsSecondPage.getResultData().getContent())
                .containsExactlyInAnyOrder(
                        ImmutableMap.of("ROW_KEY", 1, "NAME", "MERIADOCK", "RACE", "HOBBIT"),
                        ImmutableMap.of("ROW_KEY", 5, "NAME", "MRODO", "RACE", "HOBBIT"));
        soft.assertThat(datasetResultsSecondPage.getResultData().getNumber())
                .isEqualTo(1);
        soft.assertThat(datasetResultsSecondPage.getResultData().getTotalPages())
                .isEqualTo(2);
        soft.assertThat(datasetResultsSecondPage.getResultData().getNumberOfElements())
                .isEqualTo(2);
    }

    @Test
    public void findOutputDatasetRowData_MapStepWithSearch_ReturnsOnlyDatasetDataAndPaginatesResponse() {
        jdbcFixture.insertData("FELLOWSHIP", fellowshipHeaders, Arrays.asList(
                Arrays.asList(1, "MERIADOCK", "HOBBIT", 991),
                Arrays.asList(2, "PEREGUIN", "HOBBIT", 992),
                Arrays.asList(3, "SIMLI", "DWARF", 993),
                Arrays.asList(4, "SAM", "HOBBIT", 994),
                Arrays.asList(5, "MRODO", "HOBBIT", 995),
                Arrays.asList(6, "LEGOLAS", "ELF", 996)));

        Dataset fellowship = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .physicalTableName("FELLOWSHIP")
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.stringField("NAME").build(),
                                ProductPopulated.stringField("RACE").build())))
                        .build())
                .predicate("RACE = 'HOBBIT'")
                .build();

        PipelineMapStep drillBackStep = ProductPopulated.mapPipelineStep()
                .filters(newHashSet("CLUB = 'FELLOWSHIP'"))
                .build();

        FilterExpression filters = Filter.builder()
                .columnName("NAME")
                .type(FilterOption.CONTAINS)
                .filter("M")
                .build();

        DatasetRowData datasetResultsFirstPage = datasetResultRepository.findDrillbackOutputDatasetRowData(
                fellowship, drillBackStep, PageRequest.of(0, 2,
                        Sort.by(
                                Sort.Order.by("NAME")
                                        .with(Sort.Direction.DESC),
                                Sort.Order.by("RACE")
                                        .with(Sort.Direction.ASC))), filters);

        soft.assertThat(datasetResultsFirstPage.getResultData().getContent())
                .containsExactlyInAnyOrder(
                        ImmutableMap.of(
                                "ROW_KEY", 5, "NAME", "MRODO", "RACE", "HOBBIT",
                                "FCR_SYS__SCHEMA_IN_PHYSICAL__ROW_KEY", 995),
                        ImmutableMap.of("ROW_KEY", 4, "NAME", "SAM", "RACE", "HOBBIT",
                                "FCR_SYS__SCHEMA_IN_PHYSICAL__ROW_KEY", 994));
        soft.assertThat(datasetResultsFirstPage.getResultData().getNumber())
                .isEqualTo(0);
        soft.assertThat(datasetResultsFirstPage.getResultData().getTotalPages())
                .isEqualTo(2);
        soft.assertThat(datasetResultsFirstPage.getResultData().getNumberOfElements())
                .isEqualTo(2);

        List<Filter> filters2 = Collections.singletonList(Filter.builder()
                .columnName("RACE")
                .type(FilterOption.CONTAINS)
                .filter("ELF")
                .build());

        DatasetRowData datasetResultsSecondPage =
                datasetResultRepository.findDrillbackInputDatasetRowData(
                        fellowship, ProductPopulated.mapPipelineStep().filters(emptySet()).build(), PageRequest.of(1, 2,
                                Sort.by(
                                        Sort.Order.by("NAME")
                                                .with(Sort.Direction.DESC),
                                        Sort.Order.by("RACE")
                                                .with(Sort.Direction.ASC))), false, null, null);

        soft.assertThat(datasetResultsSecondPage.getResultData().getContent())
                .containsExactlyInAnyOrder(
                        ImmutableMap.of("ROW_KEY", 1, "NAME", "MERIADOCK", "RACE", "HOBBIT"),
                        ImmutableMap.of("ROW_KEY", 5, "NAME", "MRODO", "RACE", "HOBBIT"));
        soft.assertThat(datasetResultsSecondPage.getResultData().getNumber())
                .isEqualTo(1);
        soft.assertThat(datasetResultsSecondPage.getResultData().getTotalPages())
                .isEqualTo(2);
        soft.assertThat(datasetResultsSecondPage.getResultData().getNumberOfElements())
                .isEqualTo(2);
    }

    private final List<JdbcFixture.ColumnDefinition> playerHeaders = Arrays.asList(
            new JdbcFixture.ColumnDefinition(ROW_KEY.name(), "INTEGER"),
            new JdbcFixture.ColumnDefinition("ID", "INTEGER"),
            new JdbcFixture.ColumnDefinition("NAME", "VARCHAR(255)"),
            new JdbcFixture.ColumnDefinition("COUNTRY", "VARCHAR(255)")
    );

    private final List<JdbcFixture.ColumnDefinition> playerGroupHeaders = Arrays.asList(
            new JdbcFixture.ColumnDefinition(ROW_KEY.name(), "INTEGER"),
            new JdbcFixture.ColumnDefinition("COUNT", "INTEGER"),
            new JdbcFixture.ColumnDefinition("COUNTRY", "VARCHAR(255)"),
            new JdbcFixture.ColumnDefinition("FCR_SYS__PLAYER__COUNTRY", "VARCHAR(255)")
    );

    @Test
    public void findOutputDatasetRowData_AggregationStepWithShowOnlyDrillbackRows_ReturnsOnlyDatasetDataAndPaginatesResponse() {
        jdbcFixture.insertData("PLAYER", playerHeaders, Arrays.asList(
                Arrays.asList(1, 1, "Vernell Heidecker", "Afghanistan"),
                Arrays.asList(2, 2, "Collette Brandow", "Afghanistan"),
                Arrays.asList(3, 3, "Alyce Lebow", "Afghanistan"),
                Arrays.asList(4, 4, "Maudie Considine", "Azerbaijan"),
                Arrays.asList(5, 5, "Jesusa Pehrson", "Azerbaijan"),
                Arrays.asList(6, 6, "Mariette Burkes", "Brunei Darussalam"),
                Arrays.asList(7, 7, "Fatima Gailey", "Brunei Darussalam")
        ));

        jdbcFixture.insertData("PLAYER_GROUP", playerGroupHeaders, Arrays.asList(
                Arrays.asList(1000, 3, "Afghanistan", "Afghanistan"),
                Arrays.asList(2000, 2, "Azerbaijan", "Azerbaijan"),
                Arrays.asList(3000, 2, "Brunei Darussalam", "Brunei Darussalam")
        ));

        Dataset player = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .physicalTableName("PLAYER")
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.stringField("ID").build(),
                                ProductPopulated.stringField("NAME").build(),
                                ProductPopulated.stringField("COUNTRY").build())))
                        .build())
                .recordsCount(7L)
                .predicate("1=1")
                .build();

        DatasetRowData datasetResultsFirstPage =
                datasetResultRepository.findDrillbackInputDatasetRowData(
                        player,
                        ProductPopulated.aggregatePipelineStep()
                                .schemaIn(ProductPopulated.schemaDetails().physicalTableName("PLAYER").build())
                                .schemaOut(ProductPopulated.schemaDetails()
                                        .physicalTableName("PLAYER_GROUP")
                                        .build())
                                .groupings(newHashSet("COUNTRY"))
                                .build(),
                        PageRequest.of(0, 2),
                        true, 2000L, null);

        soft.assertThat(datasetResultsFirstPage.getResultData().getContent())
                .isEqualTo(Arrays.asList(
                        ImmutableMap.of(
                                "ROW_KEY", 4, "COUNTRY", "Azerbaijan", "ID", 4, "NAME", "Maudie Considine"),
                        ImmutableMap.of(
                                "ROW_KEY", 5, "COUNTRY", "Azerbaijan", "ID", 5, "NAME", "Jesusa Pehrson")));

        soft.assertThat(datasetResultsFirstPage.getResultData().getNumber())
                .isEqualTo(0);
        soft.assertThat(datasetResultsFirstPage.getResultData().getTotalPages())
                .isEqualTo(1);
        soft.assertThat(datasetResultsFirstPage.getResultData().getNumberOfElements())
                .isEqualTo(2);
    }

    @Test
    public void findOutputDatasetRowData_AggregationStepWithShowOnlyDrillbackRowsRowKeyColumnName_ReturnsOnlyDatasetDataAndPaginatesResponse() {
        jdbcFixture.insertData("PLAYER", playerHeaders, Arrays.asList(
                Arrays.asList(1, 1, "Vernell Heidecker", "Afghanistan"),
                Arrays.asList(2, 2, "Collette Brandow", "Afghanistan"),
                Arrays.asList(3, 3, "Alyce Lebow", "Afghanistan"),
                Arrays.asList(4, 4, "Maudie Considine", "Azerbaijan"),
                Arrays.asList(5, 5, "Jesusa Pehrson", "Azerbaijan"),
                Arrays.asList(6, 6, "Mariette Burkes", "Brunei Darussalam"),
                Arrays.asList(7, 7, "Fatima Gailey", "Brunei Darussalam")
        ));

        jdbcFixture.insertData("PLAYER_GROUP", playerGroupHeaders, Arrays.asList(
                Arrays.asList(1000, 3, "Afghanistan", "Afghanistan"),
                Arrays.asList(2000, 2, "Azerbaijan", "Azerbaijan"),
                Arrays.asList(3000, 2, "Brunei Darussalam", "Brunei Darussalam")
        ));

        Dataset player = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .physicalTableName("PLAYER")
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.stringField("ID").build(),
                                ProductPopulated.stringField("NAME").build(),
                                ProductPopulated.stringField("COUNTRY").build())))
                        .build())
                .recordsCount(7L)
                .predicate("1=1")
                .build();

        PipelineAggregationStep aggregationStep = ProductPopulated.aggregatePipelineStep()
                .schemaIn(ProductPopulated.schemaDetails().physicalTableName("PLAYER").build())
                .schemaOut(ProductPopulated.schemaDetails()
                        .physicalTableName("PLAYER_GROUP")
                        .build())
                .groupings(newHashSet("COUNTRY"))
                .build();

        PageRequest firstPage = PageRequest.of(0, 2, Sort.by(Sort.Order.by("ID").with(Sort.Direction.ASC)));
        PageRequest secondPage = PageRequest.of(1, 2, Sort.by(Sort.Order.by("ID").with(Sort.Direction.ASC)));

        DatasetRowData datasetResultsFirstPage = datasetResultRepository.findDrillbackInputDatasetRowData(
                player, aggregationStep, firstPage, true, 1000L, null);

        soft.assertThat(datasetResultsFirstPage.getResultData().getContent())
                .isEqualTo(Arrays.asList(
                        ImmutableMap.of("ROW_KEY", 1, "COUNTRY", "Afghanistan", "ID", 1, "NAME", "Vernell Heidecker"),
                        ImmutableMap.of("ROW_KEY", 2, "COUNTRY", "Afghanistan", "ID", 2, "NAME", "Collette Brandow")));

        soft.assertThat(datasetResultsFirstPage.getResultData().getNumber())
                .isEqualTo(0);
        soft.assertThat(datasetResultsFirstPage.getResultData().getTotalPages())
                .isEqualTo(2);
        soft.assertThat(datasetResultsFirstPage.getResultData().getNumberOfElements())
                .isEqualTo(2);

        DatasetRowData datasetResultsSecondPage = datasetResultRepository.findDrillbackInputDatasetRowData(
                player, aggregationStep, secondPage, true, 1000L, null);

        soft.assertThat(datasetResultsSecondPage.getResultData().getContent())
                .isEqualTo(Collections.singletonList(
                        ImmutableMap.of("ROW_KEY", 3, "COUNTRY", "Afghanistan", "ID", 3, "NAME", "Alyce Lebow")));

        soft.assertThat(datasetResultsSecondPage.getResultData().getNumber())
                .isEqualTo(1);
        soft.assertThat(datasetResultsSecondPage.getResultData().getTotalPages())
                .isEqualTo(2);
        soft.assertThat(datasetResultsSecondPage.getResultData().getNumberOfElements())
                .isEqualTo(1);
    }

    @Test
    public void findDrillbackInputDatasetRowData_WithFilters_ReturnsOnlyFilteredDatasetData() {
        jdbcFixture.insertData("PLAYER", playerHeaders, Arrays.asList(
                Arrays.asList(1, 1, "Vernell Heidecker", "Afghanistan"),
                Arrays.asList(2, 2, "Collette Brandow", "Afghanistan"),
                Arrays.asList(3, 3, "Alyce Lebow", "Afghanistan"),
                Arrays.asList(4, 4, "Maudie Considine", "Azerbaijan"),
                Arrays.asList(5, 5, "Jesusa Pehrson", "Azerbaijan"),
                Arrays.asList(6, 6, "Mariette Burkes", "Brunei Darussalam"),
                Arrays.asList(7, 7, "Fatima Gailey", "Brunei Darussalam")
        ));

        jdbcFixture.insertData("PLAYER_GROUP", playerGroupHeaders, Arrays.asList(
                Arrays.asList(1, 3, "Afghanistan", "Afghanistan"),
                Arrays.asList(2, 2, "Azerbaijan", "Azerbaijan"),
                Arrays.asList(3, 2, "Brunei Darussalam", "Brunei Darussalam")
        ));

        Dataset player = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .physicalTableName("PLAYER")
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.stringField("ID").build(),
                                ProductPopulated.stringField("NAME").build(),
                                ProductPopulated.stringField("COUNTRY").build())))
                        .build())
                .recordsCount(7L)
                .predicate("1=1")
                .build();

        FilterExpression countryAfghanistan = Filter.builder()
                .columnName("COUNTRY").type(FilterOption.EQUALS).filter("Afghanistan").build();

        Filter nameStartsWithM = Filter.builder()
                .columnName("NAME").type(FilterOption.STARTS_WITH).filter("M").build();

        Filter surnameStartsWithB = Filter.builder()
                .columnName("NAME").type(FilterOption.CONTAINS).filter(" B").build();

        PipelineAggregationStep pipelineStep = ProductPopulated.aggregatePipelineStep()
                .schemaIn(ProductPopulated.schemaDetails().physicalTableName("PLAYER").build())
                .schemaOut(ProductPopulated.schemaDetails()
                        .physicalTableName("PLAYER_GROUP")
                        .build())
                .groupings(newHashSet("COUNTRY"))
                .build();

        DatasetRowData afghanistanOnly =
                datasetResultRepository.findDrillbackInputDatasetRowData(
                        player, pipelineStep, Pageable.unpaged(), false, null, countryAfghanistan);

        soft.assertThat(afghanistanOnly.getResultData().getContent())
                .isEqualTo(Arrays.asList(
                        ImmutableMap.of(
                                "ROW_KEY", 1, "COUNTRY", "Afghanistan", "ID", 1, "NAME", "Vernell Heidecker"),
                        ImmutableMap.of(
                                "ROW_KEY", 2, "COUNTRY", "Afghanistan", "ID", 2, "NAME", "Collette Brandow"),
                        ImmutableMap.of(
                                "ROW_KEY", 3, "COUNTRY", "Afghanistan", "ID", 3, "NAME", "Alyce Lebow")));

        DatasetRowData afghanistanOnlyAndNameContainsB =
                datasetResultRepository.findDrillbackInputDatasetRowData(
                        player, pipelineStep, Pageable.unpaged(), false, null,
                        CombinedFilter.and(countryAfghanistan, surnameStartsWithB));

        soft.assertThat(afghanistanOnlyAndNameContainsB.getResultData().getContent())
                .isEqualTo(Collections.singletonList(
                        ImmutableMap.of(
                                "ROW_KEY", 2, "COUNTRY", "Afghanistan", "ID", 2, "NAME", "Collette Brandow")));

        DatasetRowData nameStartsWithMOrContainsB =
                datasetResultRepository.findDrillbackInputDatasetRowData(
                        player, pipelineStep, Pageable.unpaged(), false, null,
                        CombinedFilter.or(nameStartsWithM, surnameStartsWithB));

        soft.assertThat(nameStartsWithMOrContainsB.getResultData().getContent())
                .isEqualTo(Arrays.asList(
                        ImmutableMap.of(
                                "ROW_KEY", 2, "COUNTRY", "Afghanistan", "ID", 2, "NAME", "Collette Brandow"),
                        ImmutableMap.of(
                                "ROW_KEY", 4, "COUNTRY", "Azerbaijan", "ID", 4, "NAME", "Maudie Considine"),
                        ImmutableMap.of(
                                "ROW_KEY", 6, "COUNTRY", "Brunei Darussalam", "ID", 6, "NAME", "Mariette Burkes")));
    }

    private final List<JdbcFixture.ColumnDefinition> billionaireHeaders = Arrays.asList(
            new JdbcFixture.ColumnDefinition(ROW_KEY.name(), "INTEGER"),
            new JdbcFixture.ColumnDefinition("NamE", "VARCHAR(255)"),
            new JdbcFixture.ColumnDefinition("Networth_in_Billions", "INTEGER")
    );

    @Test
    public void findDrillbackInputDatasetRowData_PipelineMapStepWithFilters_ReturnsOnlyDatasetDataAndFilterValuesWithLowerCaseFilterResult() {
        jdbcFixture.insertData("BILLIONAIRES", billionaireHeaders, Arrays.asList(
                Arrays.asList(1, "JEFF BEZOS", 131),
                Arrays.asList(2, "BILL GATES", 97),
                Arrays.asList(3, "WARREN BUFFETT", 83),
                Arrays.asList(4, "BERNARD ARNAULT", 76),
                Arrays.asList(5, "CARLOS SLIM", 64),
                Arrays.asList(6, "AMANCIO ORTEGA", 63),
                Arrays.asList(7, "LARRY ELLISON", 63),
                Arrays.asList(8, "MARK ZUCKERBERG", 63),
                Arrays.asList(9, "AMANCIO ORTEGA", 63),
                Arrays.asList(10, "AMANCIO ORTEGA", 63)
        ));

        Dataset billionaires = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .physicalTableName("BILLIONAIRES")
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.stringField("NamE").build(),
                                ProductPopulated.intField("Networth_in_Billions").build())))
                        .build())
                .recordsCount(10L)
                .predicate("1=1")
                .build();

        PipelineMapStep drillBackStep = ProductPopulated.mapPipelineStep()
                .filters(newHashSet("NamE = 'JEFF BEZOS' AND Networth_in_Billions > 10"))
                .build();

        DatasetRowData datasetResultsFirstPage = datasetResultRepository.findDrillbackInputDatasetRowData(
                billionaires, drillBackStep, PageRequest.of(0, 2), false, null, null);

        soft.assertThat(datasetResultsFirstPage.getResultData().getContent())
                .containsExactlyInAnyOrder(
                        ImmutableMap.of(
                                "ROW_KEY", 1,
                                "NamE", "JEFF BEZOS",
                                "Networth_in_Billions", 131,
                                "FCR_SYS__FILTERED", false),
                        ImmutableMap.of(
                                "ROW_KEY", 2,
                                "NamE", "BILL GATES",
                                "Networth_in_Billions", 97,
                                "FCR_SYS__FILTERED", true));
        soft.assertThat(datasetResultsFirstPage.getResultData().getNumber())
                .isEqualTo(0);
        soft.assertThat(datasetResultsFirstPage.getResultData().getTotalPages())
                .isEqualTo(5);
        soft.assertThat(datasetResultsFirstPage.getResultData().getNumberOfElements())
                .isEqualTo(2);
    }

    private final List<JdbcFixture.ColumnDefinition> employeesHeaders = Arrays.asList(
            new JdbcFixture.ColumnDefinition(ROW_KEY.name(), "INTEGER"),
            new JdbcFixture.ColumnDefinition("PermOrContract", "VARCHAR(255)"),
            new JdbcFixture.ColumnDefinition("MonthlyCost", "VARCHAR(255)"),
            new JdbcFixture.ColumnDefinition("FullName", "VARCHAR(255)")
    );

    @Test
    public void findDrillbackInputDatasetRowData_PipelineMapStepWithUnionTransformation_IncludesOnlyFilterWithSameSchemaId() {
        jdbcFixture.insertData("EMPLOYEES", employeesHeaders, Arrays.asList(
                Arrays.asList(1, "Perm", "2500", "Mrs Senior Developer"),
                Arrays.asList(2, "Perm", "1700", "Mr Junior Associate"),
                Arrays.asList(3, "Contract", "5000", "Ms Very Expensive"),
                Arrays.asList(4, "Contract", "3000", "Master Average Contracter")
        ));

        Dataset employees = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .id(1L)
                        .physicalTableName("EMPLOYEES")
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.intField("ROW_KEY").build(),
                                ProductPopulated.stringField("PermOrContract").build(),
                                ProductPopulated.decimalField("MonthlyCost").build(),
                                ProductPopulated.stringField("FullName").build())))
                        .build())
                .build();

        PipelineFilter pipelineFilter1 = PipelineFilter.builder()
                .unionSchemaId(1L)
                .filter("PermOrContract = 'Perm'")
                .build();

        PipelineFilter pipelineFilter2 = PipelineFilter.builder()
                .unionSchemaId(2L)
                .filter("OneOffCost = false")
                .build();

        PipelineMapStep drillBackStep = ProductPopulated.mapPipelineStep().build();
        drillBackStep.setPipelineFilters(newHashSet(pipelineFilter1, pipelineFilter2));
        drillBackStep.setType(TransformationType.UNION);

        DatasetRowData datasetResults =
                datasetResultRepository.findDrillbackInputDatasetRowData(
                        employees,
                        drillBackStep,
                        PageRequest.of(0, 4),
                        false,
                        null,
                        null);

        soft.assertThat(datasetResults.getResultData().getContent())
                .containsExactlyInAnyOrder(
                        ImmutableMap.of(
                                "FCR_SYS__FILTERED", false,
                                "MonthlyCost", "2500",
                                "FullName", "Mrs Senior Developer",
                                "PermOrContract", "Perm",
                                "ROW_KEY", 1
                        ),
                        ImmutableMap.of(
                                "FCR_SYS__FILTERED", false,
                                "MonthlyCost", "1700",
                                "FullName", "Mr Junior Associate",
                                "PermOrContract", "Perm",
                                "ROW_KEY", 2
                        ),
                        ImmutableMap.of(
                                "FCR_SYS__FILTERED", true,
                                "MonthlyCost", "5000",
                                "FullName", "Ms Very Expensive",
                                "PermOrContract", "Contract",
                                "ROW_KEY", 3
                        ),
                        ImmutableMap.of(
                                "FCR_SYS__FILTERED", true,
                                "MonthlyCost", "3000",
                                "FullName", "Master Average Contracter",
                                "PermOrContract", "Contract",
                                "ROW_KEY", 4
                        )
                );
    }

    @Test
    public void findDrillbackInputDatasetRowData_PipelineMapStepWithFilterUsingDoubleEquals_returnsResult() {
        jdbcFixture.insertData("EMPLOYEES", employeesHeaders, Collections.singletonList(
                Arrays.asList(1, "Perm", "2500", "Mrs Senior Developer")
        ));

        Dataset employees = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .id(1L)
                        .physicalTableName("EMPLOYEES")
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.intField("ROW_KEY").build(),
                                ProductPopulated.stringField("PermOrContract").build(),
                                ProductPopulated.decimalField("MonthlyCost").build(),
                                ProductPopulated.stringField("FullName").build())))
                        .build())
                .build();

        PipelineFilter pipelineFilter1 = PipelineFilter.builder()
                .unionSchemaId(1L)
                .filter("PermOrContract == 'Perm'")
                .build();

        PipelineMapStep drillBackStep = ProductPopulated.mapPipelineStep().build();
        drillBackStep.setPipelineFilters(newHashSet(pipelineFilter1));

        DatasetRowData datasetResults =
                datasetResultRepository.findDrillbackInputDatasetRowData(
                        employees,
                        drillBackStep,
                        PageRequest.of(0, 4),
                        false,
                        null,
                        null);

        soft.assertThat(datasetResults.getResultData().getContent())
                .containsExactlyInAnyOrder(
                        ImmutableMap.of(
                                "FCR_SYS__FILTERED", false,
                                "MonthlyCost", "2500",
                                "FullName", "Mrs Senior Developer",
                                "PermOrContract", "Perm",
                                "ROW_KEY", 1
                        )
                );
    }

    @SuppressWarnings("all")
    @Test
    public void queryDataset_DatasetWithDrillbackColumns_ReturnsReturnsResultSetWithOnlySchemaFields() {
        jdbcFixture.insertData("FELLOWSHIP", fellowshipHeaders, Arrays.asList(
                Arrays.asList(1, "MERIADOCK", "HOBBIT", 991),
                Arrays.asList(2, "SAM", "HOBBIT", 992),
                Arrays.asList(3, "FRODO", "HOBBIT", 993),
                Arrays.asList(4, "PEREGUIN", "HOBBIT", 994),
                Arrays.asList(5, "GIMLI", "DWARF", 995),
                Arrays.asList(6, "LEGOLAS", "ELF", 996)));

        Dataset fellowship = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .physicalTableName("FELLOWSHIP")
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.stringField("NAME").build(),
                                ProductPopulated.stringField("RACE").build())))
                        .build())
                .predicate("RACE = 'HOBBIT'")
                .build();

        DatasetRowData datasetResultsFirstPage = datasetResultRepository.queryDataset(
                fellowship, PageRequest.of(0, 2), null);

        soft.assertThat(datasetResultsFirstPage.getResultData().getContent())
                .containsExactlyInAnyOrder(
                        ImmutableMap.of("NAME", "MERIADOCK", "RACE", "HOBBIT"),
                        ImmutableMap.of("NAME", "SAM", "RACE", "HOBBIT"));
        soft.assertThat(datasetResultsFirstPage.getResultData().getNumber())
                .isEqualTo(0);
        soft.assertThat(datasetResultsFirstPage.getResultData().getTotalPages())
                .isEqualTo(2);
        soft.assertThat(datasetResultsFirstPage.getResultData().getNumberOfElements())
                .isEqualTo(2);

        DatasetRowData datasetResultsSecondPage =
                datasetResultRepository.queryDataset(
                        fellowship,
                        PageRequest.of(1, 2),
                        null);

        soft.assertThat(datasetResultsSecondPage.getResultData().getContent())
                .containsExactlyInAnyOrder(
                        ImmutableMap.of("NAME", "FRODO", "RACE", "HOBBIT"),
                        ImmutableMap.of("NAME", "PEREGUIN", "RACE", "HOBBIT"));
        soft.assertThat(datasetResultsSecondPage.getResultData().getNumber())
                .isEqualTo(1);
        soft.assertThat(datasetResultsSecondPage.getResultData().getTotalPages())
                .isEqualTo(2);
        soft.assertThat(datasetResultsSecondPage.getResultData().getNumberOfElements())
                .isEqualTo(2);
    }

    @SuppressWarnings("all")
    @Test
    public void queryDataset_QueryDatasetWithFilter_ReturnsFilteredResultSet() {
        jdbcFixture.insertData("FELLOWSHIP", fellowshipHeaders, Arrays.asList(
                Arrays.asList(1, "MERIADOCK", "HOBBIT", 991),
                Arrays.asList(2, "SAM", "HOBBIT", 992),
                Arrays.asList(3, "FRODO", "HOBBIT", 993),
                Arrays.asList(4, "PEREGUIN", "HOBBIT", 994),
                Arrays.asList(5, "GIMLI", "DWARF", 995),
                Arrays.asList(6, "LEGOLAS", "ELF", 996),
                Arrays.asList(7, "ARAGORN", "MAN", 997),
                Arrays.asList(8, "BOROMIR", "MAN", 998),
                Arrays.asList(9, "GANDALF", "MAIA", 999)));

        Dataset fellowship = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .physicalTableName("FELLOWSHIP")
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.stringField("NAME").build(),
                                ProductPopulated.stringField("RACE").build())))
                        .build())
                .predicate("1 = 1")
                .build();

        Filter onlyMen = Filter.builder()
                .columnName("RACE")
                .type(FilterOption.EQUALS)
                .filter("MAN")
                .build();

        DatasetRowData datasetResultsFirstPage = datasetResultRepository.queryDataset(
                fellowship, PageRequest.of(0, 1, Sort.by("NAME").descending()), onlyMen);

        soft.assertThat(datasetResultsFirstPage.getResultData().getContent())
                .containsExactly(
                        ImmutableMap.of("NAME", "BOROMIR", "RACE", "MAN"));
        soft.assertThat(datasetResultsFirstPage.getResultData().getNumber())
                .isEqualTo(0);
        soft.assertThat(datasetResultsFirstPage.getResultData().getTotalPages())
                .isEqualTo(2);
        soft.assertThat(datasetResultsFirstPage.getResultData().getNumberOfElements())
                .isEqualTo(1);

        DatasetRowData datasetResultsSecondPage =
                datasetResultRepository.queryDataset(
                        fellowship, PageRequest.of(1, 1, Sort.by("NAME").descending()), onlyMen);
        ;

        soft.assertThat(datasetResultsSecondPage.getResultData().getContent())
                .containsExactly(
                        ImmutableMap.of("NAME", "ARAGORN", "RACE", "MAN"));
        soft.assertThat(datasetResultsSecondPage.getResultData().getNumber())
                .isEqualTo(1);
        soft.assertThat(datasetResultsSecondPage.getResultData().getTotalPages())
                .isEqualTo(2);
        soft.assertThat(datasetResultsSecondPage.getResultData().getNumberOfElements())
                .isEqualTo(1);
    }

    private static Date toDate(final LocalDate localDate) {
        return Timestamp.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }
}
