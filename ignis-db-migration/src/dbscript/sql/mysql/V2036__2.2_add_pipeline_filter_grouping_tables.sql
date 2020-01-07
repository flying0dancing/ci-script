CREATE TABLE PIPELINE_STEP_FILTER (
  PIPELINE_STEP_ID BIGINT NOT NULL,
  FILTER VARCHAR(255) DEFAULT '' NOT NULL,
  CONSTRAINT PIP_STEP_FILTER_FK FOREIGN KEY (PIPELINE_STEP_ID) REFERENCES PIPELINE_STEP (ID),
  CONSTRAINT PIP_STEP_FILTER_UNIQUE PRIMARY KEY (PIPELINE_STEP_ID, FILTER)
);

CREATE TABLE PIPELINE_STEP_GROUPING (
  PIPELINE_STEP_ID BIGINT NOT NULL,
  GROUPING VARCHAR(255) DEFAULT '' NOT NULL,
  CONSTRAINT PIP_STEP_GROUPING_FK FOREIGN KEY (PIPELINE_STEP_ID) REFERENCES PIPELINE_STEP (ID),
  CONSTRAINT PIP_STEP_GROUPING_UNIQUE PRIMARY KEY (PIPELINE_STEP_ID, GROUPING)
);