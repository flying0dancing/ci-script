package com.lombardrisk.ignis.design.server.productconfig.schema;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.common.json.LocalDateDeserializer;
import com.lombardrisk.ignis.common.json.LocalDateSerializer;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import com.lombardrisk.ignis.design.server.productconfig.schema.model.SchemaConstraints;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table(name = "DATASET_SCHEMA")
public class Schema implements Serializable, Identifiable {

    public static final String ENTITY_NAME = "Schema";
    private static final long serialVersionUID = -4905054048114793524L;
    private static final String REGEX_MESSAGE = "Name has to start with at least 1 word character, "
            + "followed by 0 or more word characters or '_'";
    private static final boolean DEFAULT_LATEST = true;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PRODUCT_ID")
    private Long productId;

    @Column(name = "PHYSICAL_TABLE_NAME")
    @NotNull
    @Size(min = 1, max = 31)
    @Pattern(regexp = "^[a-zA-Z]+[_\\w]*$", message = REGEX_MESSAGE)
    private String physicalTableName;

    @Column(name = "DISPLAY_NAME")
    @NotNull
    private String displayName;

    @NotNull
    @Column(name = "MAJOR_VERSION")
    private Integer majorVersion;

    @Column(name = "LATEST")
    private Boolean latest = DEFAULT_LATEST;

    @NotNull
    @Column(name = "START_DATE")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate startDate;

    @Column(name = "END_DATE")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate endDate;

    @Column(name = "CREATED_TIME")
    private Date createdTime;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Valid
    @OrderBy("id ASC")
    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "DATASET_SCHEMA_ID", nullable = false, updatable = false, insertable = false)
    @JsonDeserialize(as = LinkedHashSet.class)
    private Set<Field> fields = new LinkedHashSet<>();

    @Valid
    @OrderBy("id ASC")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "DATASET_SCHEMA_ID", nullable = false)
    private Set<ValidationRule> validationRules = new LinkedHashSet<>();

    public boolean endsAfterOtherStarts(final Schema otherSchema) {
        LocalDate otherSchemaStartDate = otherSchema.getStartDate();
        return endDate != null && endDate.isAfter(otherSchemaStartDate);
    }

    public Schema copy() {
        return Schema.builder()
                .majorVersion(majorVersion)
                .startDate(startDate)
                .endDate(endDate)
                .physicalTableName(physicalTableName)
                .displayName(displayName)
                .fields(MapperUtils.mapCollectionOrEmpty(fields, Field::copy, LinkedHashSet::new))
                .validationRules(new HashSet<>())
                .build();
    }

    SchemaConstraints getSchemaConstraints() {
        return SchemaConstraints.builder()
                .displayName(displayName)
                .physicalTableName(physicalTableName)
                .majorVersion(majorVersion)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    @SuppressWarnings({ "unused", "squid:S1068" })
    public static class SchemaBuilder {

        private Boolean latest = DEFAULT_LATEST;
    }
}
