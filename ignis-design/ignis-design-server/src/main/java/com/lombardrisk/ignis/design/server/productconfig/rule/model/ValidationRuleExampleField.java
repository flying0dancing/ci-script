package com.lombardrisk.ignis.design.server.productconfig.rule.model;

import com.lombardrisk.ignis.data.common.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "id")
@Entity
@Table(name = "VALIDATION_RULE_EXAMPLE_FIELD")
public class ValidationRuleExampleField implements Serializable, Identifiable {

    private static final long serialVersionUID = -8526773624456929999L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false, updatable = false)
    private Long id;

    @Column(name = "NAME")
    @NotNull
    private String name;

    @Column(name = "VALUE")
    private String value;

    public ValidationRuleExampleField copy() {
        return ValidationRuleExampleField.builder()
                .name(name)
                .value(value)
                .build();
    }
}


