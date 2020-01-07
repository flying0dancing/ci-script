package com.lombardrisk.ignis.design.field.fixtures;

public class FieldRepositoryFixtureTest {

//    @Test
//    public void findById_FieldExists_ReturnsValue() {
//        BooleanField field1 = DesignField.Populated.booleanField().name("field1").build();
//
//        Schema schema = schemaRepository.saveSchema(Design.Populated.schema()
//                .fields(newLinkedHashSet(asList(field1)))
//                .build());
//
//        productConfigRepository.save(Design.Populated.productConfig().tables(newHashSet(schema)).build());
//
//        Field foundField = fieldRepositoryFixture.findById(field1.getId())
//                .orElseThrow(() ->
//                        new IllegalStateException("Field " + field1.getName() + " was expected but was not found"));
//
//        assertThat(foundField).isEqualTo(field1);
//    }
//
//    @Test
//    public void findById_WrongId_ReturnsEmpty() {
//        Schema schema = schemaRepository.saveSchema(Design.Populated.schema()
//                .fields(newLinkedHashSet(asList(
//                        DesignField.Populated.booleanField().name("field1").build())))
//                .build());
//
//        productConfigRepository.save(Design.Populated.productConfig().tables(newHashSet(schema)).build());
//
//        assertThat(fieldRepositoryFixture.findById(10921).isDefined())
//                .isFalse();
//    }
}
