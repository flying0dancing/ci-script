CREATE TABLE VALIDATION_RULE_SET (
  ID NUMBER(38, 0) NOT NULL PRIMARY KEY,
  NAME        VARCHAR2(100) NOT NULL,
  DATASET_NAME VARCHAR2(255) NOT NULL
);

CREATE TABLE VALIDATION_RULE (
  ID NUMBER(38, 0) NOT NULL PRIMARY KEY,
  VALIDATION_RULE_SET_ID NUMBER(38,0) NOT NULL,
  REGULATOR_ID VARCHAR2(100),
  RULE_TYPE VARCHAR2(50),
  SEVERITY VARCHAR2(50),
  VERSION NUMBER(38,0),
  START_DATE TIMESTAMP DEFAULT NULL,
  END_DATE TIMESTAMP DEFAULT NULL,
  NAME VARCHAR2(100),
  DESCRIPTION VARCHAR2(2000),
  EXPRESSION VARCHAR2(1000),
    CONSTRAINT RULE_RULE_SET_FK FOREIGN KEY (VALIDATION_RULE_SET_ID) REFERENCES VALIDATION_RULE_SET (ID)

);