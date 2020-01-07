CREATE TABLE PIPELINE_JOIN_FIELD (
  ID BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL,
  LEFT_JOIN_FIELD_ID BIGINT NOT NULL,
  RIGHT_JOIN_FIELD_ID BIGINT NOT NULL,
  PIPELINE_JOIN_ID BIGINT NOT NULL,
  CONSTRAINT JOIN_FIELD__FK FOREIGN KEY (PIPELINE_JOIN_ID) REFERENCES PIPELINE_STEP_JOIN (ID),
  CONSTRAINT LEFT_JOIN_FIELD__FK FOREIGN KEY (LEFT_JOIN_FIELD_ID) REFERENCES DATASET_SCHEMA_FIELD (ID),
  CONSTRAINT RIGHT_JOIN_FIELD__FK FOREIGN KEY (RIGHT_JOIN_FIELD_ID) REFERENCES DATASET_SCHEMA_FIELD (ID)
);

INSERT INTO PIPELINE_JOIN_FIELD (ID, LEFT_JOIN_FIELD_ID, RIGHT_JOIN_FIELD_ID, PIPELINE_JOIN_ID)
    SELECT ID, SJ.LEFT_JOIN_FIELD_ID, SJ.RIGHT_JOIN_FIELD_ID, SJ.ID FROM PIPELINE_STEP_JOIN SJ;

ALTER TABLE PIPELINE_STEP_JOIN DROP FOREIGN KEY PIP_STEP_L_FIELD_FK;
ALTER TABLE PIPELINE_STEP_JOIN DROP FOREIGN KEY PIP_STEP_R_FIELD_FK;
ALTER TABLE PIPELINE_STEP_JOIN DROP COLUMN LEFT_JOIN_FIELD_ID;
ALTER TABLE PIPELINE_STEP_JOIN DROP COLUMN RIGHT_JOIN_FIELD_ID;