package com.lombardrisk.ignis.client.external.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lombardrisk.ignis.client.external.productconfig.export.ValidationRuleExport;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport;
import com.lombardrisk.ignis.client.external.productconfig.export.ProductConfigExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductConfigMixin {

    public static ObjectMapper initializeObjectMapper() {
        Map<Class<?>, Class<?>> mixIns = new HashMap<>();
        mixIns.put(ProductConfigExport.class, ProductConfigExportMixIn.class);
        mixIns.put(SchemaExport.class, IgnoreIdMixIn.class);
        mixIns.put(FieldExport.class, IgnoreIdMixIn.class);
        mixIns.put(ValidationRuleExport.class, IgnoreIdMixIn.class);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .setMixIns(mixIns);
        return objectMapper;
    }

    private abstract class IgnoreIdMixIn {

        @JsonIgnore
        private Long id;
        @JsonIgnore
        private Date createdTime;
        @JsonIgnore
        private boolean hasDatasets;
    }

    private abstract class ProductConfigExportMixIn extends IgnoreIdMixIn {

        @JsonIgnore
        private List<SchemaExport> tables;

        @JsonIgnore
        private String importStatus;
    }
}
