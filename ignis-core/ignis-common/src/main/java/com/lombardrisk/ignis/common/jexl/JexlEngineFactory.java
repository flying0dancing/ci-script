package com.lombardrisk.ignis.common.jexl;

import com.lombardrisk.jexl.ExpressionFunctions;
import lombok.experimental.UtilityClass;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class JexlEngineFactory {

    private static final int JEXL_CACHE_SIZE = 512;

    @SuppressWarnings("squid:S109")
    public static JexlBuilder jexlEngine() {
        Map<String, Object> namespaces = new HashMap<>(3);
        namespaces.put("number", NumberUtils.class);
        namespaces.put("string", StringUtils.class);
        namespaces.put(null, new ExpressionFunctions());

        return new JexlBuilder()
                .cache(JEXL_CACHE_SIZE)
                .strict(true)
                .silent(false)
                .debug(false)
                .namespaces(namespaces);
    }
}
