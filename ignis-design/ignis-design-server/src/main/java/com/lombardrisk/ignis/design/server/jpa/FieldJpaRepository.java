package com.lombardrisk.ignis.design.server.jpa;

import com.lombardrisk.ignis.design.field.model.Field;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FieldJpaRepository extends JpaRepository<Field, Long> {

    Optional<Field> findByNameAndSchemaId(String name, Long schemaId);
}
