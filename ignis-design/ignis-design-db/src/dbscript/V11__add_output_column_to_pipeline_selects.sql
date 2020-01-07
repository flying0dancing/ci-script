ALTER TABLE PIPELINE_STEP_SELECT ADD (
    ID NUMBER(19),
    OUTPUT_FIELD_ID NUMBER(19),
    IS_WINDOW NUMBER(1,0) DEFAULT 0
);

UPDATE PIPELINE_STEP_SELECT SET ID = "HIBERNATE_SEQUENCE".nextval;

ALTER TABLE PIPELINE_STEP_SELECT MODIFY ID NOT NULL;

ALTER TABLE PIPELINE_STEP_SELECT DROP CONSTRAINT UNIQUE_VALUE;

ALTER TABLE PIPELINE_STEP_SELECT ADD (
    CONSTRAINT PIP_STEP_SELECT_PK PRIMARY KEY (ID),
    CONSTRAINT PIP_STEP_SELECT_FIELD_FK FOREIGN KEY (OUTPUT_FIELD_ID) REFERENCES DATASET_SCHEMA_FIELD(ID)
);

CREATE TABLE PIPELINE_STEP_WINDOW_PARTITION (
    PIPELINE_STEP_SELECT_ID NUMBER(19),
    FIELD_NAME VARCHAR2(255),
    CONSTRAINT PIP_STEP_WIN_PART_FK FOREIGN KEY (PIPELINE_STEP_SELECT_ID) REFERENCES PIPELINE_STEP_SELECT(ID)
);

CREATE TABLE PIPELINE_STEP_WINDOW_ORDER (
    PIPELINE_STEP_SELECT_ID NUMBER(19),
    FIELD_NAME VARCHAR2(255),
    DIRECTION VARCHAR2(4),
    PRIORITY NUMBER(3),
    CONSTRAINT PIP_STEP_WIN_ORD_FK FOREIGN KEY (PIPELINE_STEP_SELECT_ID) REFERENCES PIPELINE_STEP_SELECT(ID)
);

CREATE TABLE PIPELINE_WINDOW_STEP (
  ID NUMBER(19, 0) NOT NULL PRIMARY KEY,
  SCHEMA_IN_ID NUMBER(19, 0) NOT NULL ,
  SCHEMA_OUT_ID NUMBER(19, 0) NOT NULL,
  CONSTRAINT WINDOW_STEP_FK FOREIGN KEY (ID) REFERENCES PIPELINE_STEP (ID),
  CONSTRAINT WINDOW_SCHEMA_IN_FK FOREIGN KEY (SCHEMA_IN_ID) REFERENCES DATASET_SCHEMA (ID),
  CONSTRAINT WINDOW_SCHEMA_OUT_FK FOREIGN KEY (SCHEMA_OUT_ID) REFERENCES DATASET_SCHEMA (ID)
);
