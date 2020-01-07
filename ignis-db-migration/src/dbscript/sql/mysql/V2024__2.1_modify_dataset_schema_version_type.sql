UPDATE DATASET_SCHEMA
SET VERSION = null;

ALTER TABLE DATASET_SCHEMA
  MODIFY VERSION INT,
DROP INDEX DATASET_SCHEMA_UNIQUE,
ADD CONSTRAINT UNIQUE_PHYS_NAME UNIQUE (PHYSICAL_TABLE_NAME, VERSION),
ADD CONSTRAINT UNIQUE_DISP_NAME UNIQUE (DISPLAY_NAME, VERSION);

UPDATE DATASET_SCHEMA
SET VERSION = 1;

ALTER TABLE DATASET_SCHEMA
  MODIFY VERSION INT NOT NULL;