package com.lombardrisk.ignis.server.product.table.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaPeriod;
import com.lombardrisk.ignis.common.json.LocalDateSerializer;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.Versionable;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
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
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@javax.persistence.Table(name = "DATASET_SCHEMA")
public class Table implements Serializable, Identifiable, Versionable<Integer> {

    private static final long serialVersionUID = -4905054048114793524L;
    private static final String REGEX_MESSAGE = "PhysicalTableName has to start with at least 1 word character, "
            + "followed by 0 or more word characters or '_'";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;


    @Column(name = "PRODUCT_ID")
    private Long productId;

    @Column(name = "PHYSICAL_TABLE_NAME")
    @NotNull
    @Size(min = 1, max = 31)
    @Pattern(regexp = "^[A-Z]+[_\\w]*$", message = REGEX_MESSAGE)
    private String physicalTableName;

    @Column(name = "DISPLAY_NAME")
    @NotNull
    private String displayName;

    @Column(name = "VERSION")
    @NotNull
    private Integer version;

    @Column(name = "START_DATE")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate startDate;

    @Column(name = "END_DATE")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate endDate;

    @Column(name = "CREATED_TIME")
    private Date createdTime;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "HAS_DATASETS")
    private Boolean hasDatasets;

    @Valid
    @OrderBy("id ASC")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "DATASET_SCHEMA_ID", nullable = false)
    @JsonDeserialize(as = LinkedHashSet.class)
    private Set<Field> fields = new LinkedHashSet<>();

    @Valid
    @OrderBy("id ASC")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "DATASET_SCHEMA_ID", nullable = false)
    private Set<ValidationRule> validationRules = new LinkedHashSet<>();

    @Override
    public String getName() {
        return physicalTableName;
    }

    public boolean isSameVersionAs(@NotNull final Table other) {
        Table otherTable = Preconditions.checkNotNull(other, "other table cannot be null");

        return this.getName().equalsIgnoreCase(otherTable.getName())
                && this.getVersion().equals(otherTable.getVersion());
    }

    @JsonIgnore
    public SchemaPeriod getSchemaPeriod() {
        return SchemaPeriod.between(startDate, endDate);
    }
}
