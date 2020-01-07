ALTER TABLE DATASET
  ADD DATASET_SCHEMA_ID BIGINT(20),
  ADD FOREIGN KEY DATASET_SCHEMA_FK(DATASET_SCHEMA_ID) REFERENCES DATASET_SCHEMA(ID);

UPDATE DATASET
  SET DATASET_SCHEMA_ID = (
		SELECT ID
		FROM DATASET_SCHEMA
		WHERE DATASET_SCHEMA.NAME = DATASET.TABLE_NAME
	);