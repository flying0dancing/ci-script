ALTER TABLE DATASET_SCHEMA
    ADD DISPLAY_NAME VARCHAR(255);

UPDATE DATASET_SCHEMA SET DISPLAY_NAME = DATASET_SCHEMA.NAME;

ALTER TABLE DATASET_SCHEMA modify DISPLAY_NAME VARCHAR(255) NOT NULL;