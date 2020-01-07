package com.lombardrisk.ignis.server.product.table;

import com.lombardrisk.ignis.server.product.table.model.Field;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FieldRepository extends JpaRepository<Field<?>, Long> {
    //no-op
}
