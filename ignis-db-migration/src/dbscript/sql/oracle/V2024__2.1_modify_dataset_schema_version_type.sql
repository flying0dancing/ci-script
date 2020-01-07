ALTER TABLE DATASET_SCHEMA
  DROP CONSTRAINT DATASET_SCHEMA_UNIQUE;

ALTER TABLE DATASET_SCHEMA
  ADD CONSTRAINT UNIQUE_PHYS_NAME UNIQUE (PHYSICAL_TABLE_NAME, VERSION);

ALTER TABLE DATASET_SCHEMA
  ADD CONSTRAINT UNIQUE_DISP_NAME UNIQUE (DISPLAY_NAME, VERSION);

UPDATE DATASET_SCHEMA
SET VERSION = null;

ALTER TABLE DATASET_SCHEMA
  MODIFY VERSION NUMBER(19, 0);

UPDATE DATASET_SCHEMA
SET VERSION = 1;

ALTER TABLE DATASET_SCHEMA
  MODIFY VERSION NUMBER(19, 0) NOT NULL;