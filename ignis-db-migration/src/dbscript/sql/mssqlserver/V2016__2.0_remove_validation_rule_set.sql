ALTER TABLE VALIDATION_RULE
  ADD DATASET_SCHEMA_ID BIGINT
  CONSTRAINT RULE_DATASET_SCHEMA_FK FOREIGN KEY (DATASET_SCHEMA_ID) REFERENCES DATASET_SCHEMA (ID);

GO

UPDATE VALIDATION_RULE
SET VALIDATION_RULE.DATASET_SCHEMA_ID = DS.ID
FROM DATASET_SCHEMA DS
  INNER JOIN VALIDATION_RULE_SET RS ON DS.VALIDATION_RULE_SET_ID = RS.ID
  INNER JOIN VALIDATION_RULE R ON RS.ID = R.VALIDATION_RULE_SET_ID
;

ALTER TABLE VALIDATION_RULE ALTER COLUMN DATASET_SCHEMA_ID BIGINT NOT NULL;

ALTER TABLE VALIDATION_RULE DROP CONSTRAINT UNIQUE_RULE;

ALTER TABLE VALIDATION_RULE
  ADD CONSTRAINT UNIQUE_RULE UNIQUE (DATASET_SCHEMA_ID, RULE_ID, VERSION, START_DATE, END_DATE);

ALTER TABLE VALIDATION_RULE DROP CONSTRAINT RULE_RULE_SET_FK;
ALTER TABLE VALIDATION_RULE DROP COLUMN VALIDATION_RULE_SET_ID;

ALTER TABLE DATASET_SCHEMA DROP CONSTRAINT VALIDATION_RULE_SET_FK;
ALTER TABLE DATASET_SCHEMA DROP COLUMN VALIDATION_RULE_SET_ID;

DROP TABLE VALIDATION_RULE_SET;