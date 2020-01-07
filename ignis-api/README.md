# Ignis Api

This module is used for sharing code between Ignis Server, Design Studio and the Spark Jobs.
Its main purpose is to keep the schema validation logic consistent between all the applications.


I.e. When developing schemas in Design Studio I want to be sure that any test data I input in Design Studio
is validated in the same way as it will be in the FCR Engine.

This is done mainly by the interface [`ValidatableField`](src/main/java/com/lombardrisk/ignis/api/table/validation/ValidatableField.java).
