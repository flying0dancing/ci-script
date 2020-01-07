package com.lombardrisk.ignis.common.jexl;

import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.junit.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class JexlEngineFactoryTest {

    @Test
    public void jexlFactory_LRMLog10Added() {
        JexlEngine jexlEngine = JexlEngineFactory.jexlEngine().create();

        Object result = jexlEngine.createScript("log10(10)")
                .execute(new MapContext());

        assertThat((BigDecimal) result)
                .isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    public void jexlFactory_StringUtilsAdded() {
        JexlEngine jexlEngine = JexlEngineFactory.jexlEngine().create();

        Object result = jexlEngine.createScript("string:isAnyEmpty('')")
                .execute(new MapContext());

        assertThat(result)
                .isEqualTo(true);
    }

    @Test
    public void jexlFactory_NumberUtilsAdded() {
        JexlEngine jexlEngine = JexlEngineFactory.jexlEngine().create();

        Object result = jexlEngine.createScript("number:toInt('100')")
                .execute(new MapContext());

        assertThat(result)
                .isEqualTo(100);
    }
}