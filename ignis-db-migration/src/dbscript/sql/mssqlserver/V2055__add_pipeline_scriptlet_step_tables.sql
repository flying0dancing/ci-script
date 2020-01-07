CREATE TABLE PIPELINE_SCRIPTLET_STEP (
  ID BIGINT NOT NULL PRIMARY KEY,
  SCHEMA_OUT_ID BIGINT NOT NULL,
  JAR_FILE VARCHAR(255) NOT NULL,
  CLASS_NAME VARCHAR(500) NOT NULL,
  CONSTRAINT SCRIPTLET_STEP_FK FOREIGN KEY (ID) REFERENCES PIPELINE_STEP (ID),
  CONSTRAINT SCRIPTLET_SCHEMA_OUT_FK FOREIGN KEY (SCHEMA_OUT_ID) REFERENCES DATASET_SCHEMA (ID)
);

CREATE TABLE PIPELINE_STEP_SCRIPTLET_INPUT (
  ID BIGINT NOT NULL PRIMARY KEY,
  PIPELINE_STEP_ID BIGINT NOT NULL,
  SCHEMA_IN_ID BIGINT NOT NULL,
  INPUT_NAME VARCHAR(255) NOT NULL,
  CONSTRAINT SCRIPTLET_STEP_INPUT_UNIQUE UNIQUE (PIPELINE_STEP_ID, SCHEMA_IN_ID),
  CONSTRAINT STEP_SCRIPTLET_PIP_STEP_FK FOREIGN KEY (PIPELINE_STEP_ID) REFERENCES PIPELINE_STEP (ID),
  CONSTRAINT STEP_SCRIPTLET_SCHEMA_IN_FK FOREIGN KEY (SCHEMA_IN_ID) REFERENCES DATASET_SCHEMA (ID)
);
