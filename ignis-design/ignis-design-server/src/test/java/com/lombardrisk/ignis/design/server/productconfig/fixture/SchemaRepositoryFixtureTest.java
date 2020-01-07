package com.lombardrisk.ignis.design.server.productconfig.fixture;

import com.lombardrisk.ignis.design.field.DesignField;
import com.lombardrisk.ignis.design.field.FieldService;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.productconfig.api.SchemaRepository;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

public class SchemaRepositoryFixtureTest {

    @Rule
    public final JUnitSoftAssertions soft = new JUnitSoftAssertions();

    private SchemaRepository schemaRepository;
    private FieldService fieldService;

    @Before
    public void setUp() {
        ProductServiceFixtureFactory productServiceFactory = ProductServiceFixtureFactory.create();
        SchemaServiceFixtureFactory schemaServiceFactory
                = SchemaServiceFixtureFactory.create();
        schemaRepository = schemaServiceFactory.getSchemaRepository();
        fieldService = schemaServiceFactory.getFieldService();
    }

    @Test
    public void save_NewFields_AddsFieldIdSequence() {
        Schema created = schemaRepository.saveSchema(Populated.schema()
                .fields(emptySet())
                .build());

        Long one = fieldService.save(created.getId(), DesignField.Populated.stringFieldRequest("one").build())
                .get().getId();
        Long two = fieldService.save(created.getId(), DesignField.Populated.stringFieldRequest("two").build())
                .get().getId();
        Long three = fieldService.save(created.getId(), DesignField.Populated.stringFieldRequest("three").build())
                .get().getId();

        assertThat(schemaRepository.findById(created.getId()).get().getFields())
                .extracting(Field::getId)
                .contains(one, two, three);
    }

    @Test
    public void save_NewSchemas_GeneratesNewIds() {
        schemaRepository.saveSchema(Populated.schema().build());
        schemaRepository.saveSchema(Populated.schema().build());
        schemaRepository.saveSchema(Populated.schema().build());
        schemaRepository.saveSchema(Populated.schema().build());
        schemaRepository.saveSchema(Populated.schema().build());
        Schema latest = schemaRepository.saveSchema(Populated.schema().build());

        assertThat(latest.getId())
                .isEqualTo(6);
    }

    @Test
    public void findById_SchemaFound_ReturnsSchema() {
        Schema favPizza = Populated.schema()
                .id(22L)
                .fields(newHashSet(
                        DesignField.Populated.stringField()
                                .name("FAV_PIZZA")
                                .nullable(true)
                                .build()))
                .build();

        Schema schema = schemaRepository.saveSchema(favPizza);

        assertThat(schemaRepository.findById(schema.getId()).get())
                .isEqualTo(favPizza);
    }

    @Test
    public void findByIdAndProductId_SchemaWithProductId_ReturnsSchema() {
        Schema favPizza = Populated.schema()
                .id(22L)
                .productId(12L)
                .fields(newHashSet(
                        DesignField.Populated.stringField()
                                .name("FAV_PIZZA")
                                .nullable(true)
                                .build()))
                .build();

        schemaRepository.saveSchema(favPizza);

        Optional<Schema> byIdAndProductId = schemaRepository.findByIdAndProductId(12L, 22L);

        assertThat(byIdAndProductId).hasValue(favPizza);
    }

    @Test
    public void findSecondLatestVersion_ThreeVersions_ReturnsSecond() {
        Schema v1 = Populated.schema().id(22L).displayName("one").majorVersion(1).latest(false).build();
        Schema v2 = Populated.schema().id(23L).displayName("one").majorVersion(2).latest(false).build();
        Schema v3 = Populated.schema().id(24L).displayName("one").majorVersion(3).latest(true).build();

        schemaRepository.saveSchema(v1);
        schemaRepository.saveSchema(v2);
        schemaRepository.saveSchema(v3);

        assertThat(schemaRepository.findSecondLatestVersion("one"))
                .hasValue(v2);
    }

    @Test
    public void findSecondLatestVersion_OneVersion_ReturnsEmpty() {
        Schema v1 = Populated.schema().id(22L).displayName("one").majorVersion(1).latest(true).build();

        schemaRepository.saveSchema(v1);

        assertThat(schemaRepository.findSecondLatestVersion("one"))
                .isEmpty();
    }

    @Test
    public void findPreviousVersion_ThirdVersion_ReturnsSecond() {
        Schema v1 = Populated.schema().id(22L).displayName("one").majorVersion(1).latest(false).build();
        Schema v2 = Populated.schema().id(23L).displayName("one").majorVersion(2).latest(false).build();
        Schema v3 = Populated.schema().id(24L).displayName("one").majorVersion(3).latest(false).build();
        Schema v4 = Populated.schema().id(25L).displayName("one").majorVersion(4).latest(true).build();

        schemaRepository.saveSchema(v1);
        schemaRepository.saveSchema(v2);
        schemaRepository.saveSchema(v3);
        schemaRepository.saveSchema(v4);

        assertThat(schemaRepository.findPreviousVersion("one", 3))
                .hasValue(v2);
    }

    @Test
    public void findPreviousVersion_OneVersion_ReturnsEmpty() {
        Schema v1 = Populated.schema().id(22L).displayName("one").majorVersion(1).latest(true).build();

        schemaRepository.saveSchema(v1);

        assertThat(schemaRepository.findPreviousVersion("one", 1))
                .isEmpty();
    }

    @Test
    public void findNextVersion_ThirdVersion_ReturnsFourth() {
        Schema v1 = Populated.schema().id(22L).displayName("one").majorVersion(1).latest(false).build();
        Schema v2 = Populated.schema().id(23L).displayName("one").majorVersion(2).latest(false).build();
        Schema v3 = Populated.schema().id(24L).displayName("one").majorVersion(3).latest(false).build();
        Schema v4 = Populated.schema().id(25L).displayName("one").majorVersion(4).latest(true).build();

        schemaRepository.saveSchema(v1);
        schemaRepository.saveSchema(v2);
        schemaRepository.saveSchema(v3);
        schemaRepository.saveSchema(v4);

        assertThat(schemaRepository.findNextVersion("one", 3))
                .hasValue(v4);
    }

    @Test
    public void findNextVersion_NoNextVersion_ReturnsEmpty() {
        Schema v1 = Populated.schema().id(22L).displayName("one").majorVersion(1).latest(false).build();
        Schema v2 = Populated.schema().id(23L).displayName("one").majorVersion(2).latest(true).build();

        schemaRepository.saveSchema(v1);
        schemaRepository.saveSchema(v2);

        assertThat(schemaRepository.findNextVersion("one", 2))
                .isEmpty();
    }

    @Test
    public void findByDisplayName_Two_ReturnsTwo() {
        Schema v1 = Populated.schema().id(22L).displayName("one").majorVersion(1).latest(false).build();
        Schema v2 = Populated.schema().id(23L).displayName("one").majorVersion(2).latest(true).build();

        schemaRepository.saveSchema(v1);
        schemaRepository.saveSchema(v2);

        assertThat(schemaRepository.findByDisplayNameAndVersion("one", 1))
                .contains(v1);
        assertThat(schemaRepository.findByDisplayNameAndVersion("one", 2))
                .contains(v2);
    }

    @Test
    public void findByDisplayName_NoneFound_ReturnsEmpty() {
        Schema v1 = Populated.schema().id(22L).displayName("one").majorVersion(1).latest(false).build();
        Schema v2 = Populated.schema().id(23L).displayName("one").majorVersion(2).latest(true).build();

        schemaRepository.saveSchema(v1);
        schemaRepository.saveSchema(v2);

        assertThat(schemaRepository.findByDisplayNameAndVersion("two", 1))
                .isEmpty();
        assertThat(schemaRepository.findByDisplayNameAndVersion("two", 2))
                .isEmpty();
    }

    @Test
    public void findAllByIds_NoneFound_ReturnsEmpty() {
        Schema v1 = Populated.schema().id(22L).build();
        Schema v2 = Populated.schema().id(23L).build();
        Schema v3 = Populated.schema().id(24L).build();

        schemaRepository.saveSchema(v1);
        schemaRepository.saveSchema(v2);
        schemaRepository.saveSchema(v3);

        assertThat(schemaRepository.findAllByIds(Arrays.asList(23L, 24L)))
                .contains(v2, v3);
    }

    @Test
    public void findAll_ReturnsAll() {
        Schema v1 = Populated.schema().id(22L).build();
        Schema v2 = Populated.schema().id(23L).build();
        Schema v3 = Populated.schema().id(24L).build();

        schemaRepository.saveSchema(v1);
        schemaRepository.saveSchema(v2);
        schemaRepository.saveSchema(v3);

        assertThat(schemaRepository.findAll())
                .contains(v1, v2, v3);
    }

    @Test
    public void findMaxVersion_ManyVersions_ReturnsLatest() {
        Schema v1 = Populated.schema().id(21L).physicalTableName("TABLE").majorVersion(1).build();
        Schema v2 = Populated.schema().id(22L).physicalTableName("TABLE").majorVersion(2).build();
        Schema v3 = Populated.schema().id(23L).physicalTableName("TABLE").majorVersion(3).build();
        Schema otherVersion3 = Populated.schema().id(13L).physicalTableName("OTHER").majorVersion(3).build();

        schemaRepository.saveSchema(v1);
        schemaRepository.saveSchema(v2);
        schemaRepository.saveSchema(v3);
        schemaRepository.saveSchema(otherVersion3);

        assertThat(schemaRepository.findMaxVersion("TABLE"))
                .hasValue(v3);
    }
}
