package com.lombardrisk.ignis.design.server.pipeline;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.design.field.DesignField;
import com.lombardrisk.ignis.design.field.FieldService;
import com.lombardrisk.ignis.design.server.DesignStudioTestConfig;
import com.lombardrisk.ignis.design.server.fixtures.Design;
import com.lombardrisk.ignis.design.server.pipeline.converter.SchemaStructTypeConverter;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfigService;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import com.lombardrisk.ignis.design.server.productconfig.schema.SchemaService;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.parser.ParseException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class PipelineStepSparkSqlExecutorIT {

    private final SchemaStructTypeConverter schemaStructTypeConverter = new SchemaStructTypeConverter();

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Autowired
    private SparkSession sparkSession;

    @Autowired
    private ProductConfigService productService;

    @Autowired
    private SchemaService schemaService;
    @Autowired
    private FieldService fieldService;

    private ProductConfig product;

    private PipelineStepSparkSqlExecutor sqlExecutor;

    @Before
    public void setUp() {
        product = VavrAssert.assertValid(productService.createProductConfig(
                Design.Populated.newProductRequest().build()))
                .getResult();

        sqlExecutor = new PipelineStepSparkSqlExecutor(sparkSession);

        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("test", ""));
    }

    @Test
    public void execute_InvalidSqlSyntax_ReturnsError() {
        Schema schemaIn =
                Design.Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_FIRST_SCHEMA")
                        .physicalTableName("MYFS")
                        .fields(newHashSet(
                                DesignField.Populated.stringField("NAME").id(1L).build(),
                                DesignField.Populated.longField("VERSION").id(2L).build()))
                        .build();

        Schema schemaOut =
                Design.Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_SECOND_SCHEMA")
                        .physicalTableName("MYSS")
                        .fields(newHashSet(DesignField.Populated.stringField("NAME_VERSION").id(101L).build()))
                        .build();

        String sql = "SELECT CONCATNAME, VERSION) AS NAME_VERSION FROM MYFS";
        Dataset<Row> dataset = sparkSession.createDataFrame(
                emptyList(),
                new StructType(new StructField[]{
                        new StructField("NAME", DataTypes.StringType, false, Metadata.empty()),
                        new StructField("VERSION", DataTypes.StringType, false, Metadata.empty()) }));

        assertThatThrownBy(() -> sqlExecutor.executeSqlNew(
                sql, singletonMap(schemaIn, dataset)))
                .isInstanceOf(ParseException.class);
    }

    @Test
    public void execute_ValidMapSql_ReturnsOutputData() {
        Schema schemaIn =
                Design.Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_FIRST_SCHEMA")
                        .physicalTableName("MYFS")
                        .fields(newLinkedHashSet(ImmutableList.of(
                                DesignField.Populated.stringField("NAME").id(1L).build(),
                                DesignField.Populated.stringField("VERSION").id(2L).build())))
                        .build();

        Schema schemaOut =
                Design.Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_SECOND_SCHEMA")
                        .physicalTableName("MYSS")
                        .fields(newHashSet(DesignField.Populated.stringField("NAME_VERSION").id(101L).build()))
                        .build();

        String sql = "SELECT CONCAT(NAME, VERSION) AS NAME_VERSION FROM MYFS";

        List<Row> schemaInData = ImmutableList.of(
                RowFactory.create("BART", "1"),
                RowFactory.create("HOMER", "2"));

        Dataset<Row> dataset = sparkSession.createDataFrame(
                schemaInData,
                new StructType(new StructField[]{
                        new StructField("NAME", DataTypes.StringType, false, Metadata.empty()),
                        new StructField("VERSION", DataTypes.StringType, false, Metadata.empty()) }));

        Dataset<Row> outputData = sqlExecutor.executeSqlNew(
                sql, singletonMap(schemaIn, dataset));

        soft.assertThat(outputData.collectAsList())
                .isEqualTo(ImmutableList.of(
                        RowFactory.create("BART1"),
                        RowFactory.create("HOMER2")));
    }

    @Test
    public void execute_ValidAggregateSql_ReturnsOutputData() {
        Schema schemaIn =
                Design.Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_FIRST_SCHEMA")
                        .physicalTableName("MYFS")
                        .fields(newLinkedHashSet(ImmutableList.of(
                                DesignField.Populated.stringField("NAME").id(1L).build(),
                                DesignField.Populated.longField("AMOUNT").id(2L).build())))
                        .build();

        Schema schemaOut =
                Design.Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_SECOND_SCHEMA")
                        .physicalTableName("MYSS")
                        .fields(newLinkedHashSet(ImmutableList.of(
                                DesignField.Populated.stringField("NAME").id(101L).build(),
                                DesignField.Populated.longField("TOTAL_AMOUNT").id(102L).nullable(true).build())))
                        .build();

        String sql = "SELECT NAME, SUM(AMOUNT) AS TOTAL_AMOUNT FROM MYFS GROUP BY NAME";

        List<Row> schemaInDataset = ImmutableList.of(
                RowFactory.create("BART", 1L),
                RowFactory.create("BART", 8L),
                RowFactory.create("HOMER", 5L));

        Dataset<Row> dataset = sparkSession.createDataFrame(
                schemaInDataset,
                new StructType(new StructField[]{
                        new StructField("NAME", DataTypes.StringType, false, Metadata.empty()),
                        new StructField("AMOUNT", DataTypes.LongType, false, Metadata.empty()) }));

        Dataset<Row> outputDataset = sqlExecutor.executeSqlNew(
                sql, singletonMap(schemaIn, dataset));

        List<Row> outputRows = outputDataset.collectAsList();
        soft.assertThat(outputRows)
                .containsExactlyInAnyOrder(
                        RowFactory.create("BART", 9L),
                        RowFactory.create("HOMER", 5L));
    }

    @Test
    public void execute_ValidJoinSql_ReturnsOutputData() {
        Schema left =
                Design.Populated.schema()
                        .productId(product.getId())
                        .displayName("Left Table")
                        .physicalTableName("LEFT")
                        .fields(newLinkedHashSet(ImmutableList.of(
                                DesignField.Populated.longField("ID").id(1L).build(),
                                DesignField.Populated.stringField("NAME").id(2L).build())))
                        .build();

        Schema right =
                Design.Populated.schema()
                        .productId(product.getId())
                        .displayName("Right Table")
                        .physicalTableName("RIGHT")
                        .fields(newLinkedHashSet(ImmutableList.of(
                                DesignField.Populated.longField("ID").id(101L).build(),
                                DesignField.Populated.doubleField("SALARY").id(102L).build())))
                        .build();

        Schema schemaOut =
                Design.Populated.schema()
                        .productId(product.getId())
                        .displayName("Output Table")
                        .physicalTableName("OUT")
                        .fields(newLinkedHashSet(ImmutableList.of(
                                DesignField.Populated.stringField("NAME").id(201L).build(),
                                DesignField.Populated.doubleField("SALARY").id(202L).build())))
                        .build();

        String sql = "SELECT LEFT.NAME AS NAME, RIGHT.SALARY AS SALARY FROM LEFT JOIN RIGHT ON LEFT.ID = RIGHT.ID";

        List<Row> leftInputRows = ImmutableList.of(
                RowFactory.create(1L, "SKINNER"),
                RowFactory.create(2L, "KRABAPAL"),
                RowFactory.create(3L, "HOOVER"),
                RowFactory.create(4L, "WILLIE"));

        List<Row> rightInputRows = ImmutableList.of(
                RowFactory.create(1L, 25000.0),
                RowFactory.create(2L, 23000.0),
                RowFactory.create(3L, 22000.0),
                RowFactory.create(4L, 15000.0));

        Dataset<Row> leftDataset = sparkSession.createDataFrame(
                leftInputRows,
                new StructType(new StructField[]{
                        new StructField("ID", DataTypes.LongType, false, Metadata.empty()),
                        new StructField("NAME", DataTypes.StringType, false, Metadata.empty()) }));

        Dataset<Row> rightDataset = sparkSession.createDataFrame(
                rightInputRows,
                new StructType(new StructField[]{
                        new StructField("ID", DataTypes.LongType, false, Metadata.empty()),
                        new StructField("SALARY", DataTypes.DoubleType, false, Metadata.empty()) }));

        Map<Schema, Dataset<Row>> inputData = ImmutableMap.of(left, leftDataset, right, rightDataset);

        Dataset<Row> outputRows = sqlExecutor.executeSqlNew(sql, inputData);

        soft.assertThat(outputRows.collectAsList())
                .containsExactlyInAnyOrder(
                        RowFactory.create("SKINNER", 25000.0),
                        RowFactory.create("KRABAPAL", 23000.0),
                        RowFactory.create("HOOVER", 22000.0),
                        RowFactory.create("WILLIE", 15000.0));
    }

}
