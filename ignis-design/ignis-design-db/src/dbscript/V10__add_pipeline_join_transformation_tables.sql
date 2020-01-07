CREATE TABLE PIPELINE_MAP_STEP (
  ID NUMBER(19, 0) NOT NULL PRIMARY KEY,
  SCHEMA_IN_ID NUMBER(19, 0) NOT NULL ,
  SCHEMA_OUT_ID NUMBER(19, 0) NOT NULL,
  CONSTRAINT MAP_STEP_FK FOREIGN KEY (ID) REFERENCES PIPELINE_STEP (ID),
  CONSTRAINT MAP_SCHEMA_IN_FK FOREIGN KEY (SCHEMA_IN_ID) REFERENCES DATASET_SCHEMA (ID),
  CONSTRAINT MAP_SCHEMA_OUT_FK FOREIGN KEY (SCHEMA_OUT_ID) REFERENCES DATASET_SCHEMA (ID)
);

INSERT INTO PIPELINE_MAP_STEP (ID, SCHEMA_IN_ID, SCHEMA_OUT_ID)
 SELECT ID, SCHEMA_IN_ID, SCHEMA_OUT_ID FROM PIPELINE_STEP WHERE TYPE = 'MAP';

CREATE TABLE PIPELINE_AGGREGATION_STEP (
  ID NUMBER(19, 0) NOT NULL PRIMARY KEY,
  SCHEMA_IN_ID NUMBER(19, 0) NOT NULL ,
  SCHEMA_OUT_ID NUMBER(19, 0) NOT NULL,
  CONSTRAINT AGG_STEP_FK FOREIGN KEY (ID) REFERENCES PIPELINE_STEP (ID),
  CONSTRAINT AGG_SCHEMA_IN_FK FOREIGN KEY (SCHEMA_IN_ID)  REFERENCES DATASET_SCHEMA (ID),
  CONSTRAINT AGG_SCHEMA_OUT_FK FOREIGN KEY (SCHEMA_OUT_ID) REFERENCES DATASET_SCHEMA (ID)
);

INSERT INTO PIPELINE_AGGREGATION_STEP (ID, SCHEMA_IN_ID, SCHEMA_OUT_ID)
 SELECT ID, SCHEMA_IN_ID, SCHEMA_OUT_ID FROM PIPELINE_STEP WHERE TYPE = 'AGGREGATION';

ALTER TABLE PIPELINE_STEP DROP CONSTRAINT SCHEMA_IN_FK;
ALTER TABLE PIPELINE_STEP DROP COLUMN SCHEMA_IN_ID;
ALTER TABLE PIPELINE_STEP DROP CONSTRAINT SCHEMA_OUT_FK;
ALTER TABLE PIPELINE_STEP DROP COLUMN SCHEMA_OUT_ID;

CREATE TABLE PIPELINE_JOIN_STEP (
  ID NUMBER(19, 0) NOT NULL PRIMARY KEY,
  SCHEMA_OUT_ID NUMBER(19, 0) NOT NULL,
  CONSTRAINT JOIN_STEP_FK FOREIGN KEY (ID) REFERENCES PIPELINE_STEP (ID),
  CONSTRAINT JOIN_SCHEMA_OUT_FK FOREIGN KEY (SCHEMA_OUT_ID) REFERENCES DATASET_SCHEMA (ID)
);

CREATE TABLE PIPELINE_STEP_JOIN(
  ID NUMBER(19, 0) NOT NULL PRIMARY KEY,
  PIPELINE_STEP_ID NUMBER(19, 0) NOT NULL,
  LEFT_SCHEMA_ID NUMBER(19, 0) NOT NULL,
  LEFT_JOIN_FIELD_ID NUMBER(19, 0) NOT NULL,
  RIGHT_SCHEMA_ID NUMBER(19, 0) NOT NULL,
  RIGHT_JOIN_FIELD_ID NUMBER(19, 0) NOT NULL,
  JOIN_TYPE VARCHAR2(255) NOT NULL,
  CONSTRAINT PIP_STEP_JOIN_FK FOREIGN KEY (PIPELINE_STEP_ID) REFERENCES PIPELINE_STEP (ID),
  CONSTRAINT PIP_STEP_L_SCHEMA_FK FOREIGN KEY (LEFT_SCHEMA_ID) REFERENCES DATASET_SCHEMA (ID),
  CONSTRAINT PIP_STEP_L_FIELD_FK FOREIGN KEY (LEFT_JOIN_FIELD_ID) REFERENCES DATASET_SCHEMA_FIELD (ID),
  CONSTRAINT PIP_STEP_R_SCHEMA_FK FOREIGN KEY (RIGHT_SCHEMA_ID) REFERENCES DATASET_SCHEMA (ID),
  CONSTRAINT PIP_STEP_R_FIELD_FK FOREIGN KEY (RIGHT_JOIN_FIELD_ID) REFERENCES DATASET_SCHEMA_FIELD (ID)
);