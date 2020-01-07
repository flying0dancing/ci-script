ALTER TABLE PIPELINE ADD PRODUCT_ID NUMBER(38,0);

ALTER TABLE PIPELINE ADD CONSTRAINT PIPELINE_PRODUCT_FK
  FOREIGN KEY (PRODUCT_ID) REFERENCES PRODUCT_CONFIG (ID);

ALTER TABLE DATASET ADD PIPELINE_JOB_ID NUMBER(38,0);
ALTER TABLE DATASET ADD PIPELINE_INVOCATION_ID NUMBER(38,0);


CREATE TABLE PIPELINE_INVOCATION (
  ID NUMBER(19, 0) NOT NULL PRIMARY KEY,
  ENTITY_CODE VARCHAR(255) DEFAULT NULL,
  REFERENCE_DATE DATE NOT NULL,
  PIPELINE_ID NUMBER(19, 0) NOT NULL,
  CONSTRAINT PIP_INVC_FK FOREIGN KEY (PIPELINE_ID) REFERENCES PIPELINE (ID)
);

CREATE TABLE PIPELINE_STEP_INVOCATION (
  ID NUMBER(19, 0) NOT NULL PRIMARY KEY,
  PIPELINE_INVOCATION_ID NUMBER(19, 0) NOT NULL,
  PIPELINE_STEP_ID NUMBER(19, 0) NOT NULL,
  CONSTRAINT PIP_S_PIP_INVC_FK FOREIGN KEY (PIPELINE_INVOCATION_ID) REFERENCES PIPELINE_INVOCATION (ID),
  CONSTRAINT PIP_INVC_PIP_STEP_FK FOREIGN KEY (PIPELINE_STEP_ID) REFERENCES PIPELINE_STEP (ID)
);