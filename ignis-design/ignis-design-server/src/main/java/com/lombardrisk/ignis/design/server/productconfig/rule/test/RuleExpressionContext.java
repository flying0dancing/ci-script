package com.lombardrisk.ignis.design.server.productconfig.rule.test;

import com.google.common.collect.ImmutableMap;
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
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static java.time.ZoneId.systemDefault;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@UtilityClass
class RuleExpressionContext {

    static final Map<Class<? extends Field>, Object> EMPTY_VALUES_BY_FIELD_CLASS =
            ImmutableMap.<Class<? extends Field>, Object>builder()
                    .put(BooleanField.class, Boolean.FALSE)
                    .put(
                            DateField.class,
                            Date.from(LocalDateTime.of(1970, 1, 1, 0, 0, 0).atZone(systemDefault()).toInstant()))
                    .put(DecimalField.class, BigDecimal.ZERO)
                    .put(DoubleField.class, 0.0d)
                    .put(FloatField.class, 0.0f)
                    .put(IntField.class, 0)
                    .put(LongField.class, 0L)
                    .put(StringField.class, EMPTY)
                    .put(TimestampField.class, Timestamp.from(Instant.EPOCH))
                    .build();

    private static final int YEARS_OFFSET = 6;
    private static final Double DOUBLE_VALUE = 120_000 + 0.32d;
    private static final long LONG_VALUE = 120_000_000_000L;

    static final String SMALL_STRING_VALUE = ".|`";
    static final Map<Class<? extends Field>, Object> LOWER_VALUES_BY_FIELD_CLASS =
            ImmutableMap.<Class<? extends Field>, Object>builder()
                    .put(BooleanField.class, Boolean.FALSE)
                    .put(DateField.class, Date.from(now().minusYears(YEARS_OFFSET).atZone(systemDefault()).toInstant()))
                    .put(DecimalField.class, BigDecimal.valueOf(-DOUBLE_VALUE))
                    .put(DoubleField.class, -DOUBLE_VALUE)
                    .put(FloatField.class, -DOUBLE_VALUE.floatValue())
                    .put(IntField.class, -DOUBLE_VALUE.intValue())
                    .put(LongField.class, -LONG_VALUE)
                    .put(StringField.class, SMALL_STRING_VALUE)
                    .put(
                            TimestampField.class,
                            Timestamp.from(now().minusYears(YEARS_OFFSET).atZone(systemDefault()).toInstant()))
                    .build();

    static final Map<Class<? extends Field>, Object> UPPER_VALUES_BY_FIELD_CLASS =
            ImmutableMap.<Class<? extends Field>, Object>builder()
                    .put(BooleanField.class, Boolean.TRUE)
                    .put(DateField.class, Date.from(now().plusYears(YEARS_OFFSET).atZone(systemDefault()).toInstant()))
                    .put(DecimalField.class, BigDecimal.valueOf(DOUBLE_VALUE))
                    .put(DoubleField.class, DOUBLE_VALUE)
                    .put(FloatField.class, DOUBLE_VALUE.floatValue())
                    .put(IntField.class, DOUBLE_VALUE.intValue())
                    .put(LongField.class, LONG_VALUE)
                    .put(
                            StringField.class,
                            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
                                    + "Suspendisse vestibulum turpis id ipsum tempus, bibendum lobortis nisi tincidunt.")
                    .put(
                            TimestampField.class,
                            Timestamp.from(now().plusYears(YEARS_OFFSET).atZone(systemDefault()).toInstant()))
                    .build();
}
