ALTER TABLE DATASET_SCHEMA
  DROP CONSTRAINT DATASET_SCHEMA_UNIQUE;

UPDATE DATASET_SCHEMA
SET VERSION = null;

GO

ALTER TABLE DATASET_SCHEMA ALTER COLUMN VERSION INT;

UPDATE DATASET_SCHEMA
SET VERSION = 1;


ALTER TABLE DATASET_SCHEMA ALTER COLUMN VERSION INT NOT NULL;

GO

ALTER TABLE DATASET_SCHEMA
  ADD CONSTRAINT UNIQUE_PHYS_NAME UNIQUE (PHYSICAL_TABLE_NAME, VERSION);

ALTER TABLE DATASET_SCHEMA
  ADD CONSTRAINT UNIQUE_DISP_NAME UNIQUE (DISPLAY_NAME, VERSION);