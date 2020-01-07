package com.lombardrisk.ignis.server.product.productconfig;

import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import org.hibernate.validator.internal.engine.path.PathImpl;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;

public class DummyConstraintViolation implements ConstraintViolation<ProductConfig> {

    private final ProductConfig productConfig;
    private final String message;

    public DummyConstraintViolation(final ProductConfig productConfig, final String message) {
        this.productConfig = productConfig;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getMessageTemplate() {
        return null;
    }

    @Override
    public ProductConfig getRootBean() {
        return productConfig;
    }

    @Override
    public Class<ProductConfig> getRootBeanClass() {
        return ProductConfig.class;
    }

    @Override
    public Object getLeafBean() {
        return productConfig;
    }

    @Override
    public Object[] getExecutableParameters() {
        return new Object[0];
    }

    @Override
    public Object getExecutableReturnValue() {
        return null;
    }

    @Override
    public Path getPropertyPath() {
        return PathImpl.createPathFromString("productConfig.name");
    }

    @Override
    public Object getInvalidValue() {
        return null;
    }

    @Override
    public ConstraintDescriptor<?> getConstraintDescriptor() {
        return null;
    }

    @Override
    public <U> U unwrap(final Class<U> aClass) {
        return null;
    }
}
