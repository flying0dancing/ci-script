package com.lombardrisk.ignis.server.product.productconfig.model;

import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.Versionable;
import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import com.lombardrisk.ignis.server.product.table.model.Table;
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
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@javax.persistence.Table(name = "PRODUCT_CONFIG")
public class ProductConfig implements Identifiable, Versionable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME")
    @NotNull
    private String name;

    @Column(name = "VERSION")
    @NotNull
    private String version;

    @Column(name = "CREATED_TIME")
    private Date createdTime;

    @Column(name = "IMPORT_STATUS")
    @Enumerated(EnumType.STRING)
    private ImportStatus importStatus;

    @Column(name = "IMPORT_REQUEST_ID")
    private Long importRequestId;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID", nullable = false, updatable = false, insertable = false)
    @Valid
    @OrderBy("displayName asc, version desc")
    private Set<Table> tables = new LinkedHashSet<>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID", nullable = false, updatable = false, insertable = false)
    @Valid
    private Set<Pipeline> pipelines = new LinkedHashSet<>();
}
