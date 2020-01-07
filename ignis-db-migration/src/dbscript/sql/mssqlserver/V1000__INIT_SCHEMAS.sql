CREATE SEQUENCE "HIBERNATE_SEQUENCE" INCREMENT BY 1 START WITH 10000 CACHE 10000 NO CYCLE;

CREATE TABLE BATCH_JOB_INSTANCE  (
	JOB_INSTANCE_ID BIGINT  NOT NULL PRIMARY KEY,
	VERSION BIGINT,
	JOB_NAME VARCHAR(100) NOT NULL,
	JOB_KEY VARCHAR(32) NOT NULL,
	constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
) ;

CREATE TABLE BATCH_JOB_EXECUTION  (
	JOB_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY,
	VERSION BIGINT,
	JOB_INSTANCE_ID BIGINT NOT NULL,
	CREATE_TIME DATETIME2 NOT NULL,
	START_TIME DATETIME2 DEFAULT NULL,
	END_TIME DATETIME2 DEFAULT NULL,
	STATUS VARCHAR(10),
	EXIT_CODE VARCHAR(2500),
	EXIT_MESSAGE VARCHAR(2500),
	LAST_UPDATED DATETIME2,
	JOB_CONFIGURATION_LOCATION VARCHAR(2500) NULL,
	constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
	references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ;

CREATE TABLE BATCH_JOB_EXECUTION_PARAMS  (
	JOB_EXECUTION_ID BIGINT NOT NULL,
	TYPE_CD VARCHAR(6) NOT NULL,
	KEY_NAME VARCHAR(100) NOT NULL,
	STRING_VAL VARCHAR(250),
	DATE_VAL DATETIME2 DEFAULT NULL,
	LONG_VAL BIGINT,
	DOUBLE_VAL FLOAT,
	IDENTIFYING CHAR(1) NOT NULL,
	constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

CREATE TABLE BATCH_STEP_EXECUTION  (
	STEP_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY,
	VERSION BIGINT NOT NULL,
	STEP_NAME VARCHAR(100) NOT NULL,
	JOB_EXECUTION_ID BIGINT NOT NULL,
	START_TIME DATETIME2 NOT NULL,
	END_TIME DATETIME2 DEFAULT NULL,
	STATUS VARCHAR(10),
	COMMIT_COUNT BIGINT,
	READ_COUNT BIGINT,
	FILTER_COUNT BIGINT,
	WRITE_COUNT BIGINT,
	READ_SKIP_COUNT BIGINT,
	WRITE_SKIP_COUNT BIGINT,
	PROCESS_SKIP_COUNT BIGINT,
	ROLLBACK_COUNT BIGINT,
	EXIT_CODE VARCHAR(2500),
	EXIT_MESSAGE VARCHAR(2500),
	LAST_UPDATED DATETIME2,
	constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT  (
	STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
	SHORT_CONTEXT VARCHAR(2500) NOT NULL,
	SERIALIZED_CONTEXT VARCHAR(MAX),
	constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
	references BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
) ;

CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT  (
	JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
	SHORT_CONTEXT VARCHAR(2500) NOT NULL,
	SERIALIZED_CONTEXT VARCHAR(MAX),
	constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

CREATE TABLE BATCH_STEP_EXECUTION_SEQ (ID BIGINT IDENTITY);
CREATE TABLE BATCH_JOB_EXECUTION_SEQ (ID BIGINT IDENTITY);
CREATE TABLE BATCH_JOB_SEQ (ID BIGINT IDENTITY);

CREATE TABLE SVC_SERVICE_REQUEST (
	ID BIGINT NOT NULL PRIMARY KEY,
	MESSAGE_ID VARCHAR(255) DEFAULT NULL,
	SENDER_SYSTEM_ID VARCHAR(255) DEFAULT NULL,
	RECEIVER_SERVICE_NAME VARCHAR(255) DEFAULT NULL,
	CORRELATION_ID VARCHAR(255) DEFAULT NULL,
	NAME VARCHAR(255) NOT NULL,
	CREATED_BY VARCHAR(255),
	YARN_APPLICATION_TRACKING_URL VARCHAR(255),
	REQUEST_MESSAGE VARCHAR(MAX),
	JOB_EXECUTION_ID BIGINT,
	IS_DELETED BIT,
	REQUEST_TYPE VARCHAR(50)
) ;

CREATE TABLE DATASET_SCHEMA (
	ID BIGINT NOT NULL PRIMARY KEY,
	NAME VARCHAR(255),
	CREATED_BY VARCHAR(50),
	CREATED_TIME DATETIME2
	constraint DATASET_SCHEMA_UNIQUE unique (NAME)
) ;

CREATE TABLE DATASET_SCHEMA_FIELD (
	ID BIGINT NOT NULL PRIMARY KEY,
	NAME VARCHAR(255),
	FIELD_TYPE VARCHAR(50),
	IS_KEY BIT,
	NULLABLE BIT,
	DATASET_SCHEMA_ID BIGINT NOT NULL,
	DATE_FORMAT VARCHAR(50),
	DEC_SCALE INT,
	DEC_PRECISION INT,
	MAX_LENGTH INTEGER,
	MIN_LENGTH INTEGER,
	REGULAR_EXPRESSION VARCHAR(100),
	constraint DATASET_SCHEMA_FIELD_UNIQUE UNIQUE (NAME,DATASET_SCHEMA_ID),
	constraint DATASET_SCHEMA_FIELD_FK foreign key (DATASET_SCHEMA_ID)
  references DATASET_SCHEMA(ID)
) ;

CREATE TABLE STAGING_DATA_SET (
	ID BIGINT NOT NULL PRIMARY KEY,
	END_TIME DATETIME2 DEFAULT NULL,
	LAST_UPDATE_TIME DATETIME2 DEFAULT NULL,
	MESSAGE VARCHAR(MAX),
	TABLE_NAME VARCHAR(100) DEFAULT NULL,
	STAGING_FILE VARCHAR(2000),
	DATASET VARCHAR(100),
	START_TIME DATETIME2 DEFAULT NULL,
	STATUS VARCHAR(20) DEFAULT NULL,
	VALIDATION_ERROR_FILE VARCHAR(2000),
	JOB_EXECUTION_ID BIGINT DEFAULT NULL,
	METADATA_KEY VARCHAR(64) NOT NULL,
	METADATA_CONTENT VARCHAR(MAX),
	constraint STAGING_DATA_SET_FK foreign key (JOB_EXECUTION_ID)
	references SVC_SERVICE_REQUEST(ID)
) ;

CREATE TABLE DATASET (
	ID BIGINT NOT NULL PRIMARY KEY,
	JOB_EXECUTION_ID BIGINT,
	NAME VARCHAR(255),
	TABLE_NAME VARCHAR(2000),
	DATASET_TYPE VARCHAR(20),
	CREATED_TIME DATETIME2,
	METADATA_KEY VARCHAR(64) NOT NULL,
	METADATA_CONTENT VARCHAR(MAX),
	RECORDS_COUNT BIGINT,
	PREDICATE VARCHAR(255),
		constraint DATASET_FK foreign key (JOB_EXECUTION_ID)
	references SVC_SERVICE_REQUEST(ID)
) ;

CREATE TABLE DATA_QUALITY_CHECK_EXECUTION (
	ID BIGINT NOT NULL PRIMARY KEY,
	JOB_EXECUTION_ID BIGINT,
	NAME VARCHAR(255),
  DESCRIPTION VARCHAR(2000),
	STATUS VARCHAR(20),
	START_TIME DATETIME2,
	LAST_UPDATE_TIME DATETIME2,
	END_TIME DATETIME2,
  FAILED_RECORD_NUMBER BIGINT,
	TARGET_FILE VARCHAR(2000),
	constraint DQC_FK foreign key (JOB_EXECUTION_ID)
	references SVC_SERVICE_REQUEST(ID)
) ;

CREATE TABLE SEC_USER (
	ID BIGINT NOT NULL PRIMARY KEY,
	USERNAME VARCHAR(255),
	PASSWORD VARCHAR(255)
  CONSTRAINT SEC_USER_USERNAME_UNIQUE_CONSTRAINT UNIQUE (USERNAME)
) ;

INSERT INTO SEC_USER (ID, USERNAME, PASSWORD)
VALUES (NEXT VALUE FOR HIBERNATE_SEQUENCE, 'InternalJobUser', '$2a$10$Sx9ZeuLzTiBJR1GKm51jJ.khLOxKTm/A/yt3DyWRaYVMa0rfV3Tei');

INSERT INTO SEC_USER (ID, USERNAME, PASSWORD)
VALUES (NEXT VALUE FOR HIBERNATE_SEQUENCE, '${ADMIN_USER}', '${ADMIN_PASSWORD}');