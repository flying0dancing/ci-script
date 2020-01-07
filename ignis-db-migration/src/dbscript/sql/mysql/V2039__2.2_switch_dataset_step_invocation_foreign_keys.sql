ALTER TABLE DATASET
  ADD PIPELINE_STEP_INVOCATION_ID BIGINT;

ALTER TABLE DATASET
 ADD CONSTRAINT DATASET_PI_FK FOREIGN KEY (PIPELINE_INVOCATION_ID) REFERENCES PIPELINE_INVOCATION (ID),
 ADD CONSTRAINT DATASET_PSI_FK FOREIGN KEY (PIPELINE_STEP_INVOCATION_ID) REFERENCES PIPELINE_STEP_INVOCATION (ID);

UPDATE DATASET
  SET PIPELINE_STEP_INVOCATION_ID = (
		SELECT ID
    FROM PIPELINE_STEP_INVOCATION
    WHERE PIPELINE_STEP_INVOCATION.OUTPUT_DATASET_ID = DATASET.ID
	);

ALTER TABLE PIPELINE_STEP_INVOCATION
  DROP FOREIGN KEY PSI_OUT_DATASET_FK,
  DROP COLUMN OUTPUT_DATASET_ID,
  ADD COLUMN STATUS VARCHAR(20) DEFAULT NULL;

ALTER TABLE PIPELINE_STEP_INVC_DATASET
  DROP FOREIGN KEY PIP_INVC_STEP_INVC_FK,
  DROP FOREIGN KEY PIP_INVC_STEP_DATASET_FK,
  DROP PRIMARY KEY,
  MODIFY DATASET_ID BIGINT NULL;

ALTER TABLE PIPELINE_STEP_INVC_DATASET
  ADD COLUMN INPUT_PIPELINE_STEP_ID BIGINT,
  ADD CONSTRAINT PIP_INVC_STEP_INVC_FK FOREIGN KEY (PIPELINE_STEP_INVOCATION_ID) REFERENCES PIPELINE_STEP_INVOCATION (ID),
  ADD CONSTRAINT PIP_INVC_STEP_DATASET_FK FOREIGN KEY (DATASET_ID) REFERENCES DATASET (ID),
  ADD CONSTRAINT PSI_INPUT_STEP_PIP_STEP_FK FOREIGN KEY (INPUT_PIPELINE_STEP_ID) REFERENCES PIPELINE_STEP (ID);

ALTER TABLE PIPELINE_INVOCATION
  ADD COLUMN SERVICE_REQUEST_ID BIGINT(20) DEFAULT NULL;

ALTER TABLE PIPELINE_INVOCATION
  ADD CONSTRAINT PI_SERVICE_REQUEST_FK FOREIGN KEY (SERVICE_REQUEST_ID) REFERENCES SVC_SERVICE_REQUEST (ID);