package com.lombardrisk.ignis.design.server.jpa;

import com.lombardrisk.ignis.design.field.model.BooleanField;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.DesignStudioTestConfig;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class FieldJpaRepositoryIT {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Autowired
    private FieldJpaRepository fieldRepository;
    @Autowired
    private SchemaJpaRepository schemaRepository;

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void createRetrieveUpdateDelete() {
        Field booleanField = BooleanField.builder()
                .name("boolean field")
                .nullable(true)
                .build();

        Field savedField = fieldRepository.save(booleanField);

        Field foundField = fieldRepository.findById(savedField.getId()).get();

        soft.assertThat(foundField.getName())
                .isEqualTo("boolean field");

        foundField.setName("updated boolean field");

        Field updatedField = fieldRepository.save(foundField);

        soft.assertThat(updatedField.getId())
                .isEqualTo(foundField.getId());
        soft.assertThat(updatedField.getName())
                .isEqualTo("updated boolean field");

        fieldRepository.delete(updatedField);

        Optional<Field> deletedField = fieldRepository.findById(updatedField.getId());

        soft.assertThat(deletedField)
                .isEmpty();
    }
}
