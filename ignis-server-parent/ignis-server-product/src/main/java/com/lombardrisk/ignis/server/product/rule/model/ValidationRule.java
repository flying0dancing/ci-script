package com.lombardrisk.ignis.server.product.rule.model;

import com.lombardrisk.ignis.api.rule.ValidationRuleSeverity;
import com.lombardrisk.ignis.api.rule.ValidationRuleType;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.Versionable;
import com.lombardrisk.ignis.server.product.table.model.Field;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = { "contextFields", "validationRuleExamples" })
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "VALIDATION_RULE")
public class ValidationRule implements Serializable, Identifiable, Versionable<Integer> {

    private static final long serialVersionUID = 4294547192224747634L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false, updatable = false)
    private Long id;

    @Column(name = "RULE_ID")
    @NotBlank
    private String ruleId;

    @Column(name = "RULE_TYPE")
    @Enumerated(EnumType.STRING)
    private ValidationRuleType validationRuleType;

    @Column(name = "SEVERITY")
    @Enumerated(EnumType.STRING)
    private ValidationRuleSeverity validationRuleSeverity;

    @Column(name = "VERSION")
    private Integer version;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "EXPRESSION")
    @NotBlank
    private String expression;

    @Valid
    @OrderBy("id ASC")
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "VALIDATION_RULE_SCHEMA_FIELD",
            joinColumns = { @JoinColumn(name = "VALIDATION_RULE_ID") },
            inverseJoinColumns = { @JoinColumn(name = "SCHEMA_FIELD_ID") }
    )
    private Set<Field> contextFields = new LinkedHashSet<>();

    @Valid
    @OrderBy("id ASC")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "VALIDATION_RULE_ID")
    private Set<ValidationRuleExample> validationRuleExamples = new LinkedHashSet<>();

    public boolean isValidFor(final LocalDate date) {
        return (startDate.isBefore(date) || startDate.equals(date))
                && (endDate.isAfter(date) || endDate.equals(date));
    }
}
