package com.lombardrisk.ignis.design.server.productconfig.export;

import com.lombardrisk.ignis.data.common.Nameable;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import java.util.Iterator;
import java.util.List;

class ProductConfigErrorResponse {

    private enum Code {
        PRODUCT_ZIP_EMPTY("Product config zip [%s] cannot be empty"),
        PRODUCT_MISSING_MANIFEST("Product config zip [%s] must have a manifest file"),
        JSON_FORMAT_INVALID("Files [%s] are formatted incorrectly"),
        PRODUCT_CONFIG_NOT_VALID("Property '%s' of %s '%s': %s"),
        PRODUCT_CONFIG_EXISTS("Product with name [%s] already exists"),
        TABLE_EXISTS("Table with name [%s] already exists");

        private final String errorMessage;

        Code(final String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    static ErrorResponse productZipEmpty(final String productConfigName) {
        return ErrorResponse.valueOf(
                String.format(Code.PRODUCT_ZIP_EMPTY.errorMessage, productConfigName),
                Code.PRODUCT_ZIP_EMPTY.name());
    }

    static ErrorResponse productMissingManifest(final String productConfigName) {
        return ErrorResponse.valueOf(
                String.format(Code.PRODUCT_MISSING_MANIFEST.errorMessage, productConfigName),
                Code.PRODUCT_MISSING_MANIFEST.name());
    }

    static ErrorResponse jsonFormatInvalid(final List<String> fileNames) {
        return ErrorResponse.valueOf(
                String.format(Code.JSON_FORMAT_INVALID.errorMessage, String.join(", ", fileNames)),
                Code.JSON_FORMAT_INVALID.name());
    }

    static ErrorResponse productConfigExists(final String productConfigName) {
        return ErrorResponse.valueOf(
                String.format(Code.PRODUCT_CONFIG_EXISTS.errorMessage, productConfigName),
                Code.PRODUCT_CONFIG_EXISTS.name());
    }

    static ErrorResponse tableExists(final String tableName) {
        return ErrorResponse.valueOf(
                String.format(Code.TABLE_EXISTS.errorMessage, tableName),
                Code.TABLE_EXISTS.name());
    }

    static ErrorResponse productConfigNotValid(final ConstraintViolation<ProductConfig> violation) {
        return ErrorResponse.valueOf(
                generateProductConfigInvalidMessage(violation),
                Code.PRODUCT_CONFIG_NOT_VALID.name());
    }

    private static String generateProductConfigInvalidMessage(final ConstraintViolation<ProductConfig> violation) {
        if (Nameable.class.isAssignableFrom(violation.getLeafBean().getClass())) {
            Iterator<Path.Node> pathIterator = violation.getPropertyPath().iterator();

            Path.Node invalidBeanProperty = pathIterator.next();
            while (pathIterator.hasNext()) {
                invalidBeanProperty = pathIterator.next();
            }

            Nameable invalidBean = (Nameable) violation.getLeafBean();
            return String.format(
                    Code.PRODUCT_CONFIG_NOT_VALID.errorMessage,
                    invalidBeanProperty.toString(),
                    invalidBean.getClass().getSimpleName(),
                    invalidBean.getName(),
                    violation.getMessage());
        }
        return violation.getPropertyPath().toString();
    }
}
