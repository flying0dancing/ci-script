package com.lombardrisk.ignis.design.server.productconfig;

import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.Nameable;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@javax.persistence.Table(name = "PRODUCT_CONFIG")
public class ProductConfig implements Identifiable, Nameable {

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

    @Column(name = "IMPORT_STATUS")
    private ImportStatus importStatus;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "PRODUCT_ID", insertable = false, updatable = false)
    @Valid
    private Set<Schema> tables = new HashSet<>();

    public enum ImportStatus {
        SUCCESS,
        ERROR
    }
}
