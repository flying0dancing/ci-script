ALTER TABLE PIPELINE_STEP_SELECT ADD (SELECTS_CLOB CLOB);
UPDATE PIPELINE_STEP_SELECT SET SELECTS_CLOB = SELECTS;
ALTER TABLE PIPELINE_STEP_SELECT DROP COLUMN SELECTS;
ALTER TABLE PIPELINE_STEP_SELECT RENAME COLUMN SELECTS_CLOB TO SELECTS;






