package com.lombardrisk.ignis.server.dataset.rule.model;

import com.lombardrisk.ignis.api.rule.SummaryStatus;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

@Getter
@Setter
@Entity
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "VALIDATION_RESULTS_SUMMARY")
public class ValidationResultsSummary implements Identifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "VALIDATION_RULE_ID")
    private ValidationRule validationRule;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "DATASET_ID")
    private Dataset dataset;

    @Column(name = "NUMBER_OF_FAILURES")
    private Long numberOfFailures;

    @Column(name = "NUMBER_OF_ERRORS")
    private Long numberOfErrors;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private SummaryStatus status;

    @Column(name = "ERROR_MESSAGE")
    private String errorMessage;

    @Column(name = "CREATED_TIME")
    private Date createdTime;
}
