package com.lombardrisk.ignis.design.field.fixtures;

import com.lombardrisk.ignis.design.field.DesignField;
import com.lombardrisk.ignis.design.field.api.DatasetSchemaFieldRepository;
import com.lombardrisk.ignis.design.field.api.FieldRepository;
import com.lombardrisk.ignis.design.field.model.DateField;
import com.lombardrisk.ignis.design.field.model.DecimalField;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.field.model.StringField;
import com.lombardrisk.ignis.design.field.model.TimestampField;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DatasetSchemaFieldRepositoryFixtureTest {

    @Rule
    public final JUnitSoftAssertions soft = new JUnitSoftAssertions();

    private FieldRepository fieldRepository;
    private DatasetSchemaFieldRepository datasetSchemaFieldRepository;

    @Before
    public void setUp() {
        FieldServiceFactory fieldServiceFactory = FieldServiceFactory.create(noop -> true);
        fieldRepository= fieldServiceFactory.getFieldRepository();
        datasetSchemaFieldRepository = fieldServiceFactory.getDatasetSchemaFieldRepository();
    }

    @Test
    public void updateNumericField_FieldWasString_UpdatesFields() {

        Field stringField = fieldRepository.save(DesignField.Populated.stringField().build());

        datasetSchemaFieldRepository
                .updateDecimalField(stringField.getId(), "FAV_NUMBER", false, 10, 6);

        Field updatedField = fieldRepository.findById(stringField.getId()).get();

        assertThat(updatedField)
                .isInstanceOf(DecimalField.class);

        DecimalField decimalField = (DecimalField) updatedField;
        soft.assertThat(decimalField.getName())
                .isEqualTo("FAV_NUMBER");
        soft.assertThat(decimalField.getPrecision())
                .isEqualTo(10);
        soft.assertThat(decimalField.getScale())
                .isEqualTo(6);
        soft.assertThat(decimalField.isNullable())
                .isEqualTo(false);
    }

    @Test
    public void updateStringField_FieldWasDecimal_UpdatesFields() {
        Field decimalField = fieldRepository.save(DesignField.Populated.decimalField().build());

        datasetSchemaFieldRepository
                .updateStringField(decimalField.getId(), "FAV_PIZZA", false, 10, 6, "reggie");

        Field updatedField = fieldRepository.findById(decimalField.getId()).get();

        assertThat(updatedField)
                .isInstanceOf(StringField.class);

        StringField stringField = (StringField) updatedField;
        soft.assertThat(stringField.getName())
                .isEqualTo("FAV_PIZZA");
        soft.assertThat(stringField.getMaxLength())
                .isEqualTo(10);
        soft.assertThat(stringField.getMinLength())
                .isEqualTo(6);
        soft.assertThat(stringField.getRegularExpression())
                .isEqualTo("reggie");
        soft.assertThat(stringField.isNullable())
                .isEqualTo(false);
    }

    @Test
    public void updateDateField_FieldWasDecimal_UpdatesFields() {

        Field decimalField = fieldRepository.save(DesignField.Populated.decimalField().build());

        datasetSchemaFieldRepository
                .updateDateField(decimalField.getId(), "MY_BIRTHDAY", false, "date", "dd/MM/yyyy");

        Field updatedField = fieldRepository.findById(decimalField.getId()).get();

        assertThat(updatedField)
                .isInstanceOf(DateField.class);

        DateField dateField = (DateField) updatedField;
        soft.assertThat(dateField.getName())
                .isEqualTo("MY_BIRTHDAY");
        soft.assertThat(dateField.getFormat())
                .isEqualTo("dd/MM/yyyy");
        soft.assertThat(dateField.isNullable())
                .isEqualTo(false);
    }

    @Test
    public void updateTimestampField_FieldWasDecimal_UpdatesFields() {
        Field decimalField = fieldRepository.save(DesignField.Populated.decimalField().build());

        datasetSchemaFieldRepository
                .updateDateField(decimalField.getId(), "MY_BIRTHDAY", false, "timestamp", "dd/MM/yyyy");

        Field updatedField = fieldRepository.findById(decimalField.getId()).get();

        assertThat(updatedField)
                .isInstanceOf(TimestampField.class);

        TimestampField dateField = (TimestampField) updatedField;
        soft.assertThat(dateField.getName())
                .isEqualTo("MY_BIRTHDAY");
        soft.assertThat(dateField.getFormat())
                .isEqualTo("dd/MM/yyyy");
        soft.assertThat(dateField.isNullable())
                .isEqualTo(false);
    }
}
