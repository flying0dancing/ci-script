
CREATE TABLE PIPELINE_STEP_TEST (
  ID NUMBER(19, 0) NOT NULL PRIMARY KEY,
  NAME VARCHAR(255) DEFAULT NULL,
  DESCRIPTION VARCHAR(2500) DEFAULT NULL,
  PIPELINE_STEP_ID NUMBER(19, 0) NOT NULL,
  CONSTRAINT PIPELINE_STEP_FK FOREIGN KEY (PIPELINE_STEP_ID) REFERENCES PIPELINE_STEP (ID)
);

CREATE TABLE STEP_TEST_ROW (
  ID NUMBER(19, 0) NOT NULL PRIMARY KEY,
  STEP_TEST_ID NUMBER(19, 0) NOT NULL,
  SCHEMA_ID NUMBER(19, 0) NOT NULL,
  IS_RUN NUMBER(1,0) DEFAULT 0,
  STATUS VARCHAR(10) DEFAULT NULL,
  TYPE VARCHAR(10) DEFAULT NULL,
  DESCRIPTION VARCHAR(2500) DEFAULT NULL,
  CONSTRAINT STEP_TEST_FK FOREIGN KEY (STEP_TEST_ID) REFERENCES PIPELINE_STEP_TEST (ID)
);

CREATE TABLE STEP_TEST_ROW_CELL (
  ID NUMBER(19, 0) NOT NULL PRIMARY KEY,
  STEP_TEST_ROW_ID NUMBER(19, 0) NOT NULL,
  FIELD_ID NUMBER(19, 0) NOT NULL,
  DATA VARCHAR(255) DEFAULT NULL,
  CONSTRAINT STEP_TEST_ROW_FK FOREIGN KEY (STEP_TEST_ROW_ID) REFERENCES STEP_TEST_ROW (ID),
  CONSTRAINT STEP_TEST_FIELD_FK FOREIGN KEY (FIELD_ID) REFERENCES DATASET_SCHEMA_FIELD (ID)
);

/*
Note this will fail due to checksum mismatch if V13 has already run before.
In that case manually create the below sequence changing 'START WITH 1' to 'START WITH NEXT_CELL_ID'
where NEXT_CELL_ID is SELECT MAX(ID)+1 FROM STEP_TEST_ROW_CELL. Then run flyway repair.
 */
CREATE SEQUENCE "CELL_ID_SEQUENCE" INCREMENT BY 50 START WITH 1 CACHE 10000 NOCYCLE;