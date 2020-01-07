CREATE TABLE VALIDATION_RULE_EXAMPLE (
  ID                 NUMBER(38, 0) NOT NULL PRIMARY KEY,
  VALIDATION_RULE_ID NUMBER(38, 0),
  EXPECTED_RESULT    VARCHAR2(5)   NOT NULL,
  CONSTRAINT V_RULE_EXAMPLE_TO_V_RULE_FK FOREIGN KEY (VALIDATION_RULE_ID) REFERENCES VALIDATION_RULE (ID)
);

CREATE TABLE VALIDATION_RULE_EXAMPLE_FIELD (
  ID                         NUMBER(38, 0) NOT NULL PRIMARY KEY,
  VALIDATION_RULE_EXAMPLE_ID NUMBER(38, 0),
  NAME                       VARCHAR2(255),
  VALUE                      VARCHAR2(500),
  CONSTRAINT V_R_EXMPL_FIELD_V_R_EXMPL_FK FOREIGN KEY (VALIDATION_RULE_EXAMPLE_ID) REFERENCES VALIDATION_RULE_EXAMPLE (ID)
);