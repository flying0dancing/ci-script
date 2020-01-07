alter table PIPELINE_STEP_SELECT add IS_UNION BIT default 0;

alter table PIPELINE_STEP_SELECT add UNION_INPUT_SCHEMA_ID BIGINT NULL;

alter table PIPELINE_STEP_SELECT
	add constraint PIPELINE_STEP_SELECT___UNION
		foreign key (UNION_INPUT_SCHEMA_ID) references DATASET_SCHEMA (ID);
GO

CREATE TABLE PIPELINE_UNION_STEP (
  ID BIGINT NOT NULL PRIMARY KEY,
  SCHEMA_OUT_ID BIGINT NOT NULL,
  CONSTRAINT UNION_STEP_FK FOREIGN KEY (ID) REFERENCES PIPELINE_STEP (ID),
  CONSTRAINT UNION_SCHEMA_OUT_FK FOREIGN KEY (SCHEMA_OUT_ID) REFERENCES DATASET_SCHEMA (ID)
);

CREATE TABLE PIPELINE_STEP_UNION (
  PIPELINE_STEP_ID BIGINT NOT NULL,
  SCHEMA_IN_ID BIGINT NOT NULL,
  CONSTRAINT SCHEMA_IN_UNIQUE_FOR_STEP UNIQUE (PIPELINE_STEP_ID, SCHEMA_IN_ID),
  CONSTRAINT STEP_UNION_PIP_STEP_FK FOREIGN KEY (PIPELINE_STEP_ID) REFERENCES PIPELINE_UNION_STEP (ID),
  CONSTRAINT STEP_UNION_SCHEMA_IN_FK FOREIGN KEY (SCHEMA_IN_ID) REFERENCES DATASET_SCHEMA (ID)
);

-- Update filter table to have primary key id column

alter table PIPELINE_STEP_FILTER add ID BIGINT NULL;
GO

UPDATE PIPELINE_STEP_FILTER SET ID = NEXT VALUE FOR HIBERNATE_SEQUENCE;
GO

alter table PIPELINE_STEP_FILTER ALTER COLUMN ID BIGINT NOT NULL;
GO

alter table PIPELINE_STEP_FILTER drop constraint PIP_STEP_FILTER_FK;
alter table PIPELINE_STEP_FILTER drop constraint PIP_STEP_FILTER_UNIQUE;

GO

alter table PIPELINE_STEP_FILTER
	add constraint PIPELINE_STEP_FILTER_pk
		primary key (ID);

alter table PIPELINE_STEP_FILTER
	add constraint PIP_STEP_FILTER_UNIQUE
		unique (PIPELINE_STEP_ID, FILTER);

alter table PIPELINE_STEP_FILTER
    add constraint PIP_STEP_FILTER_FK FOREIGN KEY (PIPELINE_STEP_ID) REFERENCES PIPELINE_STEP (ID);

-- Add optional union schema column to filter

alter table PIPELINE_STEP_FILTER add UNION_INPUT_SCHEMA_ID BIGINT NULL;

alter table PIPELINE_STEP_FILTER
	add constraint PIPELINE_STEP_FILTER___UNION
		foreign key (UNION_INPUT_SCHEMA_ID) references DATASET_SCHEMA (ID);

GO