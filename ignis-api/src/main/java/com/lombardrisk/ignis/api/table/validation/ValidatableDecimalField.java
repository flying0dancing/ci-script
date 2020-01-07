package com.lombardrisk.ignis.api.table.validation;

import io.vavr.control.Validation;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.util.CsvContext;

import java.math.BigDecimal;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.apache.commons.lang3.math.NumberUtils.isParsable;

public interface ValidatableDecimalField extends ValidatableField<BigDecimal> {

    Integer getScale();

    Integer getPrecision();

    default Validation<String, String> doValidate(final String decimalValue) {
        if (isParsable(decimalValue)) {
            return checkPrecisionAndValue(decimalValue);
        }
        return Validation.invalid(
                String.format(
                        "\"%s\"",
                        "Field [" + getName() + "] expected to be have a decimal type, "
                                + "but was [" + decimalValue + "]"));
    }

    default Validation<String, String> checkPrecisionAndValue(final String decimalValue) {
        String wholePartDigits = decimalValue.split("\\.")[0];

        Integer precision = getPrecision();
        Integer scale = getScale();

        if (precision >= wholePartDigits.length() + scale) {
            return Validation.valid(decimalValue);
        }
        String invalidDecimal = wholePartDigits + "." + repeat("x", scale);

        return Validation.invalid(
                String.format(
                        "\"%s\"",
                        "Field ["
                                + getName()
                                + "] cannot be converted to decimal type: "
                        + "precision [" + precision + "] and scale [" + scale
                        + "] cannot be applied for value [" + decimalValue + "], "
                        + "because the resulting number (" + invalidDecimal + ") "
                                + "will not fit the precision ("
                                + precision
                                + " < "
                                + (invalidDecimal.length() - 1)
                                + ")"));
    }

    default BigDecimal doParse(final String value) {
        return new BigDecimal(value);
    }

    class ParseDecimal extends CellProcessorAdaptor implements StringCellProcessor {

        @Override
        public <T> T execute(final Object value, final CsvContext context) {
            String strValue = (String) value;

            if (isEmpty(strValue)) {
                return null;
            }
            BigDecimal bigDecimalValue = new BigDecimal((String) value);
            return next.execute(bigDecimalValue, context);
        }
    }
}
