ALTER TABLE VALIDATION_RULE
  CHANGE COLUMN REGULATOR_ID RULE_ID VARCHAR(100);

ALTER TABLE DATASET_SCHEMA
  ADD VERSION VARCHAR(15);