package com.lombardrisk.ignis.design.field;

import com.lombardrisk.ignis.client.design.schema.field.FieldDto;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.common.reservedWord.ReservedWord;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.field.api.FieldDependencyRepository;
import com.lombardrisk.ignis.design.field.fixtures.FieldServiceFactory;
import com.lombardrisk.ignis.design.field.model.BooleanField;
import com.lombardrisk.ignis.design.field.model.DateField;
import com.lombardrisk.ignis.design.field.model.DecimalField;
import com.lombardrisk.ignis.design.field.model.DoubleField;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.field.model.FloatField;
import com.lombardrisk.ignis.design.field.model.IntField;
import com.lombardrisk.ignis.design.field.model.LongField;
import com.lombardrisk.ignis.design.field.model.StringField;
import com.lombardrisk.ignis.design.field.model.TimestampField;
import com.lombardrisk.ignis.design.field.request.FieldRequest;
import io.vavr.control.Either;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.lombardrisk.ignis.design.field.DesignField.Populated.booleanFieldRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

public class FieldServiceTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    private FieldService fieldService;
    private FieldDependencyRepository fieldDependencyRepository;

    @Before
    public void setUp() {
        FieldServiceFactory fieldServiceFactory = FieldServiceFactory.create((noop) -> true);
        fieldService = fieldServiceFactory.getFieldService();
        fieldDependencyRepository = fieldServiceFactory.getMockFieldDependencyRepository();
    }

    @Test
    public void save_NewDecimalField_ReturnsFieldDto() {
        Either<ErrorResponse, FieldDto> result = fieldService.save(
                -100L,
                DesignField.Populated.decimalFieldRequest("decimal123")
                        .scale(10)
                        .precision(2)
                        .nullable(true)
                        .build());

        assertThat(result.isRight())
                .isTrue();

        FieldDto createdField = result.get();
        assertThat(createdField)
                .isInstanceOf(FieldDto.DecimalFieldDto.class);

        FieldDto.DecimalFieldDto createdDecimalField = (FieldDto.DecimalFieldDto) createdField;
        soft.assertThat(createdDecimalField.getId())
                .isNotNull();
        soft.assertThat(createdDecimalField.getName())
                .isEqualTo("decimal123");
        soft.assertThat(createdDecimalField.isNullable())
                .isTrue();
        soft.assertThat(createdDecimalField.getScale())
                .isEqualTo(10);
        soft.assertThat(createdDecimalField.getPrecision())
                .isEqualTo(2);
    }

    @Test
    public void findById_NewDecimalField_ReturnsFieldDto() {
        Either<ErrorResponse, FieldDto> result = fieldService.save(
                -100L,
                DesignField.Populated.decimalFieldRequest("decimal123")
                        .scale(10)
                        .precision(2)
                        .nullable(true)
                        .build());

        assertThat(result.isRight())
                .isTrue();

        FieldDto createdField = fieldService.findOne(result.get().getId()).get();
        assertThat(createdField)
                .isInstanceOf(FieldDto.DecimalFieldDto.class);

        FieldDto.DecimalFieldDto createdDecimalField = (FieldDto.DecimalFieldDto) createdField;
        soft.assertThat(createdDecimalField.getId())
                .isNotNull();
        soft.assertThat(createdDecimalField.getName())
                .isEqualTo("decimal123");
        soft.assertThat(createdDecimalField.isNullable())
                .isTrue();
        soft.assertThat(createdDecimalField.getScale())
                .isEqualTo(10);
        soft.assertThat(createdDecimalField.getPrecision())
                .isEqualTo(2);
    }

    @Test
    public void save_NewDateField_ReturnsFieldDto() {
        Either<ErrorResponse, FieldDto> result = fieldService.save(
                -100L,
                DesignField.Populated.dateFieldRequest("date123")
                        .nullable(true)
                        .format("yy-mm-dd")
                        .build());

        assertThat(result.isRight())
                .isTrue();

        FieldDto createdField = result.get();
        assertThat(createdField)
                .isInstanceOf(FieldDto.DateFieldDto.class);

        FieldDto.DateFieldDto createdDateField = (FieldDto.DateFieldDto) createdField;
        soft.assertThat(createdDateField.getId())
                .isNotNull();
        soft.assertThat(createdDateField.getName())
                .isEqualTo("date123");
        soft.assertThat(createdDateField.isNullable())
                .isTrue();
        soft.assertThat(createdDateField.getFormat())
                .isEqualTo("yy-mm-dd");
    }

    @Test
    public void findOne_NewDateField_ReturnsFieldDto() {
        Either<ErrorResponse, FieldDto> result = fieldService.save(
                -100L,
                DesignField.Populated.dateFieldRequest("date123")
                        .nullable(true)
                        .format("yy-mm-dd")
                        .build());

        assertThat(result.isRight())
                .isTrue();

        FieldDto createdField = fieldService.findOne(result.get().getId()).get();
        assertThat(createdField)
                .isInstanceOf(FieldDto.DateFieldDto.class);

        FieldDto.DateFieldDto createdDateField = (FieldDto.DateFieldDto) createdField;
        soft.assertThat(createdDateField.getId())
                .isNotNull();
        soft.assertThat(createdDateField.getName())
                .isEqualTo("date123");
        soft.assertThat(createdDateField.isNullable())
                .isTrue();
        soft.assertThat(createdDateField.getFormat())
                .isEqualTo("yy-mm-dd");
    }

    @Test
    public void save_NewTimestampField_ReturnsFieldDto() {
        Either<ErrorResponse, FieldDto> result = fieldService.save(
                -100L,
                DesignField.Populated.timestampFieldRequest("timestamp123")
                        .nullable(true)
                        .format("yy-mm-dd")
                        .build());

        assertThat(result.isRight())
                .isTrue();

        FieldDto createdField = result.get();
        assertThat(createdField)
                .isInstanceOf(FieldDto.TimestampFieldDto.class);

        FieldDto.TimestampFieldDto createdTimestampField = (FieldDto.TimestampFieldDto) createdField;
        soft.assertThat(createdTimestampField.getId())
                .isNotNull();
        soft.assertThat(createdTimestampField.getName())
                .isEqualTo("timestamp123");
        soft.assertThat(createdTimestampField.isNullable())
                .isTrue();
        soft.assertThat(createdTimestampField.getFormat())
                .isEqualTo("yy-mm-dd");
    }

    @Test
    public void findOne_NewTimestampField_ReturnsFieldDto() {
        Either<ErrorResponse, FieldDto> result = fieldService.save(
                -100L,
                DesignField.Populated.timestampFieldRequest("timestamp123")
                        .nullable(true)
                        .format("yy-mm-dd")
                        .build());

        assertThat(result.isRight())
                .isTrue();

        FieldDto createdField = fieldService.findOne(result.get().getId()).get();
        assertThat(createdField)
                .isInstanceOf(FieldDto.TimestampFieldDto.class);

        FieldDto.TimestampFieldDto createdTimestampField = (FieldDto.TimestampFieldDto) createdField;
        soft.assertThat(createdTimestampField.getId())
                .isNotNull();
        soft.assertThat(createdTimestampField.getName())
                .isEqualTo("timestamp123");
        soft.assertThat(createdTimestampField.isNullable())
                .isTrue();
        soft.assertThat(createdTimestampField.getFormat())
                .isEqualTo("yy-mm-dd");
    }

    @Test
    public void save_NewStringField_ReturnsFieldDto() {
        Either<ErrorResponse, FieldDto> result = fieldService.save(
                -100L,
                DesignField.Populated.stringFieldRequest("string123")
                        .nullable(true)
                        .maxLength(60)
                        .minLength(10)
                        .regularExpression("xyz")
                        .build());

        assertThat(result.isRight())
                .isTrue();

        FieldDto createdField = result.get();
        assertThat(createdField)
                .isInstanceOf(FieldDto.StringFieldDto.class);

        FieldDto.StringFieldDto createdStringField = (FieldDto.StringFieldDto) createdField;
        soft.assertThat(createdStringField.getId())
                .isNotNull();
        soft.assertThat(createdStringField.getName())
                .isEqualTo("string123");
        soft.assertThat(createdStringField.isNullable())
                .isTrue();
        soft.assertThat(createdStringField.getMaxLength())
                .isEqualTo(60);
        soft.assertThat(createdStringField.getMinLength())
                .isEqualTo(10);
        soft.assertThat(createdStringField.getRegularExpression())
                .isEqualTo("xyz");
    }

    @Test
    public void findOne_NewStringField_ReturnsFieldDto() {
        Either<ErrorResponse, FieldDto> result = fieldService.save(
                -100L,
                DesignField.Populated.stringFieldRequest("string123")
                        .nullable(true)
                        .maxLength(60)
                        .minLength(10)
                        .regularExpression("xyz")
                        .build());

        assertThat(result.isRight())
                .isTrue();

        FieldDto createdField = fieldService.findOne(result.get().getId()).get();
        assertThat(createdField)
                .isInstanceOf(FieldDto.StringFieldDto.class);

        FieldDto.StringFieldDto createdStringField = (FieldDto.StringFieldDto) createdField;
        soft.assertThat(createdStringField.getId())
                .isNotNull();
        soft.assertThat(createdStringField.getName())
                .isEqualTo("string123");
        soft.assertThat(createdStringField.isNullable())
                .isTrue();
        soft.assertThat(createdStringField.getMaxLength())
                .isEqualTo(60);
        soft.assertThat(createdStringField.getMinLength())
                .isEqualTo(10);
        soft.assertThat(createdStringField.getRegularExpression())
                .isEqualTo("xyz");
    }

    @Test
    public void save_NewIntField_ReturnsFieldDto() {
        Either<ErrorResponse, FieldDto> result = fieldService.save(
                -100L,
                DesignField.Populated.intFieldRequest("int123")
                        .nullable(true)
                        .build());

        assertThat(result.isRight())
                .isTrue();

        FieldDto createdField = result.get();
        assertThat(createdField)
                .isInstanceOf(FieldDto.IntegerFieldDto.class);

        FieldDto.IntegerFieldDto createdIntField = (FieldDto.IntegerFieldDto) createdField;
        soft.assertThat(createdIntField.getId())
                .isNotNull();
        soft.assertThat(createdIntField.getName())
                .isEqualTo("int123");
        soft.assertThat(createdIntField.isNullable())
                .isTrue();
    }

    @Test
    public void findOne_NewIntField_ReturnsFieldDto() {
        Either<ErrorResponse, FieldDto> result = fieldService.save(
                -100L,
                DesignField.Populated.intFieldRequest("int123")
                        .nullable(true)
                        .build());

        assertThat(result.isRight())
                .isTrue();

        FieldDto createdField = fieldService.findOne(result.get().getId()).get();
        assertThat(createdField)
                .isInstanceOf(FieldDto.IntegerFieldDto.class);

        FieldDto.IntegerFieldDto createdIntField = (FieldDto.IntegerFieldDto) createdField;
        soft.assertThat(createdIntField.getId())
                .isNotNull();
        soft.assertThat(createdIntField.getName())
                .isEqualTo("int123");
        soft.assertThat(createdIntField.isNullable())
                .isTrue();
    }

    @Test
    public void save_NewLongField_ReturnsFieldDto() {
        Either<ErrorResponse, FieldDto> result = fieldService.save(
                -100L,
                DesignField.Populated.longFieldRequest("long123")
                        .nullable(true)
                        .build());

        assertThat(result.isRight())
                .isTrue();

        FieldDto createdField = result.get();
        assertThat(createdField)
                .isInstanceOf(FieldDto.LongFieldDto.class);

        FieldDto.LongFieldDto createdLongField = (FieldDto.LongFieldDto) createdField;
        soft.assertThat(createdLongField.getId())
                .isNotNull();
        soft.assertThat(createdLongField.getName())
                .isEqualTo("long123");
        soft.assertThat(createdLongField.isNullable())
                .isTrue();
    }

    @Test
    public void findOne_NewLongField_ReturnsFieldDto() {
        Either<ErrorResponse, FieldDto> result = fieldService.save(
                -100L,
                DesignField.Populated.longFieldRequest("long123")
                        .nullable(true)
                        .build());

        assertThat(result.isRight())
                .isTrue();

        FieldDto createdField = fieldService.findOne(result.get().getId()).get();
        assertThat(createdField)
                .isInstanceOf(FieldDto.LongFieldDto.class);

        FieldDto.LongFieldDto createdLongField = (FieldDto.LongFieldDto) createdField;
        soft.assertThat(createdLongField.getId())
                .isNotNull();
        soft.assertThat(createdLongField.getName())
                .isEqualTo("long123");
        soft.assertThat(createdLongField.isNullable())
                .isTrue();
    }

    @Test
    public void save_NewFloatField_ReturnsFieldDto() {
        Either<ErrorResponse, FieldDto> result = fieldService.save(
                -100L,
                DesignField.Populated.floatFieldRequest("float123")
                        .nullable(true)
                        .build());

        assertThat(result.isRight())
                .isTrue();

        FieldDto createdField = result.get();
        assertThat(createdField)
                .isInstanceOf(FieldDto.FloatFieldDto.class);

        FieldDto.FloatFieldDto createdFloatField = (FieldDto.FloatFieldDto) createdField;
        soft.assertThat(createdFloatField.getId())
                .isNotNull();
        soft.assertThat(createdFloatField.getName())
                .isEqualTo("float123");
        soft.assertThat(createdFloatField.isNullable())
                .isTrue();
    }

    @Test
    public void findOne_NewFloatField_ReturnsFieldDto() {
        Either<ErrorResponse, FieldDto> result = fieldService.save(
                -100L,
                DesignField.Populated.floatFieldRequest("float123")
                        .nullable(true)
                        .build());

        assertThat(result.isRight())
                .isTrue();

        FieldDto createdField = fieldService.findOne(result.get().getId()).get();
        assertThat(createdField)
                .isInstanceOf(FieldDto.FloatFieldDto.class);

        FieldDto.FloatFieldDto createdFloatField = (FieldDto.FloatFieldDto) createdField;
        soft.assertThat(createdFloatField.getId())
                .isNotNull();
        soft.assertThat(createdFloatField.getName())
                .isEqualTo("float123");
        soft.assertThat(createdFloatField.isNullable())
                .isTrue();
    }

    @Test
    public void save_NewDoubleField_ReturnsFieldDto() {
        Either<ErrorResponse, FieldDto> result = fieldService.save(
                -100L,
                DesignField.Populated.doubleFieldRequest("double123")
                        .nullable(true)
                        .build());

        assertThat(result.isRight())
                .isTrue();

        FieldDto createdField = result.get();
        assertThat(createdField)
                .isInstanceOf(FieldDto.DoubleFieldDto.class);

        FieldDto.DoubleFieldDto createdDoubleField = (FieldDto.DoubleFieldDto) createdField;
        soft.assertThat(createdDoubleField.getId())
                .isNotNull();
        soft.assertThat(createdDoubleField.getName())
                .isEqualTo("double123");
        soft.assertThat(createdDoubleField.isNullable())
                .isTrue();
    }

    @Test
    public void findOne_NewDoubleField_ReturnsFieldDto() {
        Either<ErrorResponse, FieldDto> result = fieldService.save(
                -100L,
                DesignField.Populated.doubleFieldRequest("double123")
                        .nullable(true)
                        .build());

        assertThat(result.isRight())
                .isTrue();

        FieldDto createdField = fieldService.findOne(result.get().getId()).get();
        assertThat(createdField)
                .isInstanceOf(FieldDto.DoubleFieldDto.class);

        FieldDto.DoubleFieldDto createdDoubleField = (FieldDto.DoubleFieldDto) createdField;
        soft.assertThat(createdDoubleField.getId())
                .isNotNull();
        soft.assertThat(createdDoubleField.getName())
                .isEqualTo("double123");
        soft.assertThat(createdDoubleField.isNullable())
                .isTrue();
    }

    @Test
    public void save_NewBooleanField_ReturnsFieldDto() {
        Either<ErrorResponse, FieldDto> result = fieldService.save(
                -100L,
                DesignField.Populated.booleanFieldRequest("boolean123")
                        .nullable(true)
                        .build());

        assertThat(result.isRight())
                .isTrue();

        FieldDto createdField = result.get();
        assertThat(createdField)
                .isInstanceOf(FieldDto.BooleanFieldDto.class);

        FieldDto.BooleanFieldDto createdBooleanField = (FieldDto.BooleanFieldDto) createdField;
        soft.assertThat(createdBooleanField.getId())
                .isNotNull();
        soft.assertThat(createdBooleanField.getName())
                .isEqualTo("boolean123");
        soft.assertThat(createdBooleanField.isNullable())
                .isTrue();
    }

    @Test
    public void findOne_NewBooleanField_ReturnsFieldDto() {
        Either<ErrorResponse, FieldDto> result = fieldService.save(
                -100L,
                DesignField.Populated.booleanFieldRequest("boolean123")
                        .nullable(true)
                        .build());

        assertThat(result.isRight())
                .isTrue();

        FieldDto createdField = fieldService.findOne(result.get().getId()).get();
        assertThat(createdField)
                .isInstanceOf(FieldDto.BooleanFieldDto.class);

        FieldDto.BooleanFieldDto createdBooleanField = (FieldDto.BooleanFieldDto) createdField;
        soft.assertThat(createdBooleanField.getId())
                .isNotNull();
        soft.assertThat(createdBooleanField.getName())
                .isEqualTo("boolean123");
        soft.assertThat(createdBooleanField.isNullable())
                .isTrue();
    }

    @Test
    public void update_UpdateExistingDateField_UpdatesDateFieldInSchema() {
        FieldDto existingField = fieldService.save(
                -100L,
                DesignField.Populated.dateFieldRequest("date123")
                        .nullable(true)
                        .format("yy-mm-dd")
                        .build())
                .get();

        Either<ErrorResponse, FieldDto> result =
                fieldService.update(existingField.getId(), DesignField.Populated.dateFieldRequest()
                        .id(existingField.getId())
                        .name("changed")
                        .nullable(false)
                        .format("yyyy-mm-dd")
                        .build());

        Field createdField = fieldService.findById(result.get().getId()).get();
        assertThat(createdField)
                .isInstanceOf(DateField.class);

        DateField createdDateField = (DateField) createdField;
        soft.assertThat(createdDateField.getId())
                .isEqualTo(existingField.getId());
        soft.assertThat(createdDateField.getName())
                .isEqualTo("changed");
        soft.assertThat(createdDateField.isNullable())
                .isFalse();
        soft.assertThat(createdDateField.getFormat())
                .isEqualTo("yyyy-mm-dd");
    }

    @Test
    public void update_FieldAlreadyExistsWithName_ReturnsError() {
        FieldDto existingField = fieldService.save(
                -100L,
                DesignField.Populated.dateFieldRequest("date123")
                        .nullable(true)
                        .format("yy-mm-dd")
                        .build())
                .get();
        FieldDto otherExistingField = fieldService.save(
                -100L,
                DesignField.Populated.dateFieldRequest("date456")
                        .nullable(true)
                        .format("yy-mm-dd")
                        .build())
                .get();

        Either<ErrorResponse, FieldDto> result =
                fieldService.update(existingField.getId(), DesignField.Populated.dateFieldRequest()
                        .id(existingField.getId())
                        .name("date456")
                        .nullable(false)
                        .format("yyyy-mm-dd")
                        .build());

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft())
                .isEqualTo(ErrorResponse.valueOf("A field with name date456 already exists", "Field is not unique"));
    }

    @Test
    public void update_UpdateExistingTimestampField_UpdatesTimestampFieldInSchema() {
        FieldDto existingField = fieldService.save(
                -100L,
                DesignField.Populated.timestampFieldRequest("timestamp123")
                        .nullable(true)
                        .format("yy-mm-dd")
                        .build())
                .get();

        fieldService.update(existingField.getId(), DesignField.Populated.timestampFieldRequest()
                .id(existingField.getId())
                .name("changed")
                .nullable(false)
                .format("yyyy-mm-dd")
                .build());

        Field createdField = fieldService.findById(existingField.getId()).get();
        assertThat(createdField)
                .isInstanceOf(TimestampField.class);

        TimestampField createdTimestampField = (TimestampField) createdField;
        soft.assertThat(createdTimestampField.getId())
                .isEqualTo(existingField.getId());
        soft.assertThat(createdTimestampField.getName())
                .isEqualTo("changed");
        soft.assertThat(createdTimestampField.isNullable())
                .isFalse();
        soft.assertThat(createdTimestampField.getFormat())
                .isEqualTo("yyyy-mm-dd");
    }

    @Test
    public void update_UpdateExistingStringField_UpdatesStringFieldInSchema() {
        FieldDto existingField = fieldService.save(
                -100L,
                DesignField.Populated.stringFieldRequest("string123")
                        .nullable(true)
                        .maxLength(40)
                        .minLength(30)
                        .regularExpression("xyz")
                        .build())
                .get();

        fieldService.update(existingField.getId(), DesignField.Populated.stringFieldRequest()
                .id(existingField.getId())
                .name("changed")
                .nullable(false)
                .maxLength(100)
                .minLength(10)
                .regularExpression("new xyz")
                .build());

        Field createdField = fieldService.findById(existingField.getId()).get();
        assertThat(createdField)
                .isInstanceOf(StringField.class);

        StringField createdStringField = (StringField) createdField;
        soft.assertThat(createdStringField.getId())
                .isEqualTo(existingField.getId());
        soft.assertThat(createdStringField.getName())
                .isEqualTo("changed");
        soft.assertThat(createdStringField.isNullable())
                .isFalse();
        soft.assertThat(createdStringField.getMaxLength())
                .isEqualTo(100);
        soft.assertThat(createdStringField.getMinLength())
                .isEqualTo(10);
        soft.assertThat(createdStringField.getRegularExpression())
                .isEqualTo("new xyz");
    }

    @Test
    public void update_UpdateExistingIntField_UpdatesIntFieldInSchema() {
        FieldDto existingField = fieldService.save(
                -100L,
                DesignField.Populated.intFieldRequest("int123")
                        .nullable(true)
                        .build())
                .get();

        fieldService.update(existingField.getId(), DesignField.Populated.intFieldRequest()
                .id(existingField.getId())
                .name("changed")
                .nullable(false)
                .build());

        Field createdField = fieldService.findById(existingField.getId()).get();
        assertThat(createdField)
                .isInstanceOf(IntField.class);

        IntField createdIntField = (IntField) createdField;
        soft.assertThat(createdIntField.getId())
                .isEqualTo(existingField.getId());
        soft.assertThat(createdIntField.getName())
                .isEqualTo("changed");
        soft.assertThat(createdIntField.isNullable())
                .isFalse();
    }

    @Test
    public void update_UpdateExistingLongField_UpdatesLongFieldInSchema() {
        FieldDto existingField = fieldService.save(
                -100L,
                DesignField.Populated.longFieldRequest("long123")
                        .nullable(true)
                        .build())
                .get();

        fieldService.update(existingField.getId(), DesignField.Populated.longFieldRequest()
                .id(existingField.getId())
                .name("changed")
                .nullable(false)
                .build());

        Field createdField = fieldService.findById(existingField.getId()).get();
        assertThat(createdField)
                .isInstanceOf(LongField.class);

        LongField createdLongField = (LongField) createdField;
        soft.assertThat(createdLongField.getId())
                .isEqualTo(existingField.getId());
        soft.assertThat(createdLongField.getName())
                .isEqualTo("changed");
        soft.assertThat(createdLongField.isNullable())
                .isFalse();
    }

    @Test
    public void update_UpdateExistingFloatField_UpdatesFloatFieldInSchema() {
        FieldDto existingField = fieldService.save(
                -100L,
                DesignField.Populated.floatFieldRequest("float123")
                        .nullable(true)
                        .build())
                .get();

        fieldService.update(existingField.getId(), DesignField.Populated.floatFieldRequest()
                .id(existingField.getId())
                .name("changed")
                .nullable(false)
                .build());

        Field createdField = fieldService.findById(existingField.getId()).get();
        assertThat(createdField)
                .isInstanceOf(FloatField.class);

        FloatField createdFloatField = (FloatField) createdField;
        soft.assertThat(createdFloatField.getId())
                .isEqualTo(existingField.getId());
        soft.assertThat(createdFloatField.getName())
                .isEqualTo("changed");
        soft.assertThat(createdFloatField.isNullable())
                .isFalse();
    }

    @Test
    public void update_UpdateExistingDoubleField_UpdatesDoubleFieldInSchema() {
        FieldDto existingField = fieldService.save(
                -100L,
                DesignField.Populated.doubleFieldRequest("double123")
                        .nullable(true)
                        .build())
                .get();

        fieldService.update(existingField.getId(), DesignField.Populated.doubleFieldRequest()
                .id(existingField.getId())
                .name("changed")
                .nullable(false)
                .build());

        Field createdField = fieldService.findById(existingField.getId()).get();
        assertThat(createdField)
                .isInstanceOf(DoubleField.class);

        DoubleField createdDoubleField = (DoubleField) createdField;
        soft.assertThat(createdDoubleField.getId())
                .isEqualTo(existingField.getId());
        soft.assertThat(createdDoubleField.getName())
                .isEqualTo("changed");
        soft.assertThat(createdDoubleField.isNullable())
                .isFalse();
    }

    @Test
    public void update_UpdateExistingBooleanField_UpdatesBooleanFieldInSchema() {
        FieldDto existingField = fieldService.save(
                -100L,
                DesignField.Populated.booleanFieldRequest("boolean123")
                        .nullable(true)
                        .build())
                .get();

        fieldService.update(existingField.getId(), DesignField.Populated.booleanFieldRequest()
                .id(existingField.getId())
                .name("changed")
                .nullable(false)
                .build());

        Field createdField = fieldService.findById(existingField.getId()).get();
        assertThat(createdField)
                .isInstanceOf(BooleanField.class);

        BooleanField createdBooleanField = (BooleanField) createdField;
        soft.assertThat(createdBooleanField.getId())
                .isEqualTo(existingField.getId());
        soft.assertThat(createdBooleanField.getName())
                .isEqualTo("changed");
        soft.assertThat(createdBooleanField.isNullable())
                .isFalse();
    }

    @Test
    public void update_UpdateExistingFieldToDecimalField_UpdatesFieldInSchemaToDecimalField() {
        FieldDto existingField = fieldService.save(
                -100L,
                DesignField.Populated.intFieldRequest("int123")
                        .nullable(true)
                        .build())
                .get();

        fieldService.update(existingField.getId(), DesignField.Populated.decimalFieldRequest()
                .id(existingField.getId())
                .name("changedToDecimal")
                .nullable(false)
                .precision(40)
                .scale(2)
                .build());

        Field createdField = fieldService.findById(existingField.getId()).get();
        assertThat(createdField)
                .isInstanceOf(DecimalField.class);

        DecimalField createdDecimalField = (DecimalField) createdField;
        soft.assertThat(createdDecimalField.getId())
                .isEqualTo(existingField.getId());
        soft.assertThat(createdDecimalField.getName())
                .isEqualTo("changedToDecimal");
        soft.assertThat(createdDecimalField.isNullable())
                .isFalse();
        soft.assertThat(createdDecimalField.getPrecision())
                .isEqualTo(40);
        soft.assertThat(createdDecimalField.getScale())
                .isEqualTo(2);
    }

    @Test
    public void update_UpdateExistingFieldToDateField_UpdatesFieldInSchemaToDateField() {
        FieldDto existingField = fieldService.save(
                -100L,
                DesignField.Populated.decimalFieldRequest("decimal123")
                        .scale(10)
                        .precision(2)
                        .nullable(true)
                        .build())
                .get();

        fieldService.update(existingField.getId(), DesignField.Populated.dateFieldRequest()
                .id(existingField.getId())
                .name("changedToDate")
                .nullable(false)
                .format("yy-mm-dd")
                .build());

        Field createdField = fieldService.findById(existingField.getId()).get();
        assertThat(createdField)
                .isInstanceOf(DateField.class);

        DateField createdDateField = (DateField) createdField;
        soft.assertThat(createdDateField.getId())
                .isEqualTo(existingField.getId());
        soft.assertThat(createdDateField.getName())
                .isEqualTo("changedToDate");
        soft.assertThat(createdDateField.isNullable())
                .isFalse();
        soft.assertThat(createdDateField.getFormat())
                .isEqualTo("yy-mm-dd");
    }

    @Test
    public void update_UpdateExistingFieldToTimestampField_UpdatesFieldInSchemaToTimestampField() {
        FieldDto existingField = fieldService.save(
                -100L,
                DesignField.Populated.decimalFieldRequest("decimal123")
                        .scale(10)
                        .precision(2)
                        .nullable(true)
                        .build())
                .get();

        fieldService.update(existingField.getId(), DesignField.Populated.timestampFieldRequest()
                .id(existingField.getId())
                .name("changedToTimestamp")
                .nullable(false)
                .format("yy-mm-dd")
                .build());

        Field createdField = fieldService.findById(existingField.getId()).get();
        assertThat(createdField)
                .isInstanceOf(TimestampField.class);

        TimestampField createdTimestampField = (TimestampField) createdField;
        soft.assertThat(createdTimestampField.getId())
                .isEqualTo(existingField.getId());
        soft.assertThat(createdTimestampField.getName())
                .isEqualTo("changedToTimestamp");
        soft.assertThat(createdTimestampField.isNullable())
                .isFalse();
        soft.assertThat(createdTimestampField.getFormat())
                .isEqualTo("yy-mm-dd");
    }

    @Test
    public void update_UpdateExistingFieldToStringField_UpdatesFieldInSchemaToStringField() {
        FieldDto existingField = fieldService.save(
                -100L,
                DesignField.Populated.decimalFieldRequest("decimal123")
                        .scale(10)
                        .precision(2)
                        .nullable(true)
                        .build())
                .get();

        fieldService.update(existingField.getId(), DesignField.Populated.stringFieldRequest()
                .id(existingField.getId())
                .name("changedToString")
                .nullable(false)
                .maxLength(50)
                .minLength(3)
                .regularExpression("min|max")
                .build());

        Field createdField = fieldService.findById(existingField.getId()).get();
        assertThat(createdField)
                .isInstanceOf(StringField.class);

        StringField createdStringField = (StringField) createdField;
        soft.assertThat(createdStringField.getId())
                .isEqualTo(existingField.getId());
        soft.assertThat(createdStringField.getName())
                .isEqualTo("changedToString");
        soft.assertThat(createdStringField.isNullable())
                .isFalse();
        soft.assertThat(createdStringField.getMaxLength())
                .isEqualTo(50);
        soft.assertThat(createdStringField.getMinLength())
                .isEqualTo(3);
        soft.assertThat(createdStringField.getRegularExpression())
                .isEqualTo("min|max");
    }

    @Test
    public void update_UpdateExistingFieldToIntField_UpdatesFieldInSchemaToIntField() {
        FieldDto existingField = fieldService.save(
                -100L,
                DesignField.Populated.decimalFieldRequest("decimal123")
                        .scale(10)
                        .precision(2)
                        .nullable(true)
                        .build())
                .get();

        fieldService.update(existingField.getId(), DesignField.Populated.intFieldRequest()
                .id(existingField.getId())
                .name("changedToInt")
                .nullable(false)
                .build());

        Field createdField = fieldService.findById(existingField.getId()).get();
        assertThat(createdField)
                .isInstanceOf(IntField.class);

        IntField createdIntField = (IntField) createdField;
        soft.assertThat(createdIntField.getId())
                .isEqualTo(existingField.getId());
        soft.assertThat(createdIntField.getName())
                .isEqualTo("changedToInt");
        soft.assertThat(createdIntField.isNullable())
                .isFalse();
    }

    @Test
    public void update_UpdateExistingFieldToLongField_UpdatesFieldInSchemaToLongField() {
        FieldDto existingField = fieldService.save(
                -100L,
                DesignField.Populated.decimalFieldRequest("decimal123")
                        .scale(10)
                        .precision(2)
                        .nullable(true)
                        .build())
                .get();

        fieldService.update(existingField.getId(), DesignField.Populated.longFieldRequest()
                .id(existingField.getId())
                .name("changedToLong")
                .nullable(false)
                .build());

        Field createdField = fieldService.findById(existingField.getId()).get();
        assertThat(createdField)
                .isInstanceOf(LongField.class);

        LongField createdLongField = (LongField) createdField;
        soft.assertThat(createdLongField.getId())
                .isEqualTo(existingField.getId());
        soft.assertThat(createdLongField.getName())
                .isEqualTo("changedToLong");
        soft.assertThat(createdLongField.isNullable())
                .isFalse();
    }

    @Test
    public void update_UpdateExistingFieldToFloatField_UpdatesFieldInSchemaToFloatField() {
        FieldDto existingField = fieldService.save(
                -100L,
                DesignField.Populated.decimalFieldRequest("decimal123")
                        .scale(10)
                        .precision(2)
                        .nullable(true)
                        .build())
                .get();

        fieldService.update(existingField.getId(), DesignField.Populated.floatFieldRequest()
                .id(existingField.getId())
                .name("changedToFloat")
                .nullable(false)
                .build());

        Field createdField = fieldService.findById(existingField.getId()).get();
        assertThat(createdField)
                .isInstanceOf(FloatField.class);

        FloatField createdFloatField = (FloatField) createdField;
        soft.assertThat(createdFloatField.getId())
                .isEqualTo(existingField.getId());
        soft.assertThat(createdFloatField.getName())
                .isEqualTo("changedToFloat");
        soft.assertThat(createdFloatField.isNullable())
                .isFalse();
    }

    @Test
    public void update_UpdateExistingFieldToDoubleField_UpdatesFieldInSchemaToDoubleField() {
        FieldDto existingField = fieldService.save(
                -100L,
                DesignField.Populated.decimalFieldRequest("decimal123")
                        .scale(10)
                        .precision(2)
                        .nullable(true)
                        .build())
                .get();

        fieldService.update(existingField.getId(), DesignField.Populated.doubleFieldRequest()
                .id(existingField.getId())
                .name("changedToDouble")
                .nullable(false)
                .build());

        Field createdField = fieldService.findById(existingField.getId()).get();
        assertThat(createdField)
                .isInstanceOf(DoubleField.class);

        DoubleField createdDoubleField = (DoubleField) createdField;
        soft.assertThat(createdDoubleField.getId())
                .isEqualTo(existingField.getId());
        soft.assertThat(createdDoubleField.getName())
                .isEqualTo("changedToDouble");
        soft.assertThat(createdDoubleField.isNullable())
                .isFalse();
    }

    @Test
    public void update_UpdateExistingFieldToBooleanField_UpdatesFieldInSchemaToBooleanField() {
        FieldDto existingField = fieldService.save(
                -100L,
                DesignField.Populated.decimalFieldRequest("decimal123")
                        .scale(10)
                        .precision(2)
                        .nullable(true)
                        .build())
                .get();

        fieldService.update(existingField.getId(), DesignField.Populated.booleanFieldRequest()
                .id(existingField.getId())
                .name("changedToBoolean")
                .nullable(false)
                .build());

        Field createdField = fieldService.findById(existingField.getId()).get();
        assertThat(createdField)
                .isInstanceOf(BooleanField.class);

        BooleanField createdBooleanField = (BooleanField) createdField;
        soft.assertThat(createdBooleanField.getId())
                .isEqualTo(existingField.getId());
        soft.assertThat(createdBooleanField.getName())
                .isEqualTo("changedToBoolean");
        soft.assertThat(createdBooleanField.isNullable())
                .isFalse();
    }

    @Test
    public void update_FieldNameWithReservedWord_ShouldReturnError() {
        FieldDto existingField = fieldService.save(
                -100L,
                DesignField.Populated.decimalFieldRequest("decimal123")
                        .scale(10)
                        .precision(2)
                        .nullable(true)
                        .build())
                .get();

        Either<ErrorResponse, FieldDto> updatedFieldEither = fieldService.update(existingField.getId(), DesignField.Populated.timestampFieldRequest()
                .id(existingField.getId())
                .name(ReservedWord.EXPLAIN.getValue())
                .nullable(false)
                .build());

        assertThat(updatedFieldEither.getLeft())
                .isEqualTo(ErrorResponse.valueOf(
                        "EXPLAIN is a reserved word not allowed in a field name", "CONSTRAINT_VIOLATION"));
    }

    @Test
    public void update_FieldNameStartsWithFCR_SYS_ShouldReturnError() {
        FieldDto existingField = fieldService.save(
                -100L,
                DesignField.Populated.decimalFieldRequest("decimal123")
                        .scale(10)
                        .precision(2)
                        .nullable(true)
                        .build())
                .get();

        Either<ErrorResponse, FieldDto> updatedFieldEither =
                fieldService.update(existingField.getId(), DesignField.Populated.timestampFieldRequest()
                        .id(existingField.getId())
                        .name("FCR_SYSAnyValue")
                        .nullable(false)
                        .build());

        assertThat(updatedFieldEither.getLeft())
                .isEqualTo(ErrorResponse.valueOf(
                        "FCR_SYS is a reserved word not allowed in a field name", "CONSTRAINT_VIOLATION"));
    }

    @Test
    public void save_SchemaNotFound_ShouldReturnError() {
        FieldService fieldServiceNoSchema = FieldServiceFactory.create(meh -> false)
                .getFieldService();

        Either<ErrorResponse, FieldDto> savedFieldEither
                = fieldServiceNoSchema.save(123L, booleanFieldRequest().name("BOOL_FIELD").build());

        assertThat(savedFieldEither.getLeft())
                .isEqualTo(CRUDFailure.notFoundIds("Schema", 123L).toErrorResponse());
    }

    @Test
    public void save_FieldNameWithReservedWord_ShouldReturnError() {
        Either<ErrorResponse, FieldDto> savedFieldEither
                = fieldService.save(123L, booleanFieldRequest().name(ReservedWord.DECLARE.getValue()).build());

        assertThat(savedFieldEither.getLeft())
                .isEqualTo(ErrorResponse.valueOf(
                        "DECLARE is a reserved word not allowed in a field name", "CONSTRAINT_VIOLATION"));
    }

    @Test
    public void save_FieldNameStartsWithFCR_SYS_ShouldReturnError() {
        Either<ErrorResponse, FieldDto> savedFieldEither
                = fieldService.save(123L, booleanFieldRequest().name("FCR_SYSAynTextValue").build());

        assertThat(savedFieldEither.getLeft())
                .isEqualTo(ErrorResponse.valueOf(
                        "FCR_SYS is a reserved word not allowed in a field name", "CONSTRAINT_VIOLATION"));
    }

    @Test
    public void save_FieldNotUnique_ShouldReturnError() {
        FieldRequest.BooleanFieldRequest fieldRequest = booleanFieldRequest("EXISTING_BOOL_FIELD").build();

        Either<ErrorResponse, FieldDto> result = fieldService.save(-100L, fieldRequest);

        assertThat(result.isRight())
                .isTrue();

        FieldDto createdField = result.get();
        assertThat(createdField)
                .isInstanceOf(FieldDto.BooleanFieldDto.class);

        Either<ErrorResponse, FieldDto> savedFieldEither = fieldService.save(-100L, fieldRequest);

        assertThat(savedFieldEither.getLeft())
                .isEqualTo(ErrorResponse.valueOf(
                        "A field with name EXISTING_BOOL_FIELD already exists", "Field is not unique"));
    }

    @Test
    public void delete_DeletesFieldsAndDependencies() {
        FieldDto existingField = fieldService.save(
                -100L,
                DesignField.Populated.decimalFieldRequest("decimal123")
                        .scale(10)
                        .precision(2)
                        .nullable(true)
                        .build())
                .get();

        Field fieldToDelete = VavrAssert.assertValid(
                fieldService.deleteWithValidation(existingField.getId()))
                .getResult();

        assertThat(fieldService.findById(existingField.getId()).isDefined())
                .isFalse();

        verify(fieldDependencyRepository).deleteForField(fieldToDelete);
    }
}
