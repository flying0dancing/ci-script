package com.lombardrisk.ignis.design.server.productconfig.rule.model;

import com.lombardrisk.ignis.data.common.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.lombardrisk.ignis.common.MapperUtils.mapSet;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "id")
@Entity
@Table(name = "VALIDATION_RULE_EXAMPLE")
public class ValidationRuleExample implements Serializable, Identifiable {

    private static final long serialVersionUID = 3339492916028283684L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false, updatable = false)
    private Long id;

    @Column(name = "EXPECTED_RESULT")
    @Enumerated(EnumType.STRING)
    @NotNull
    private TestResult expectedResult;

    @OrderBy("id ASC")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "VALIDATION_RULE_EXAMPLE_ID")
    @Valid
    @NotNull
    private Set<ValidationRuleExampleField> validationRuleExampleFields = new LinkedHashSet<>();

    public ValidationRuleExample copy() {
        return ValidationRuleExample.builder()
                .expectedResult(expectedResult)
                .validationRuleExampleFields(mapSet(validationRuleExampleFields, ValidationRuleExampleField::copy))
                .build();
    }

}
