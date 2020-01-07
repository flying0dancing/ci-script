CREATE TABLE VALIDATION_RULE_EXAMPLE (
  ID                 BIGINT     NOT NULL PRIMARY KEY AUTO_INCREMENT,
  VALIDATION_RULE_ID BIGINT,
  EXPECTED_RESULT    VARCHAR(5) NOT NULL,
  CONSTRAINT V_RULE_EXAMPLE_TO_V_RULE_FK FOREIGN KEY (VALIDATION_RULE_ID) REFERENCES VALIDATION_RULE (ID)
);

CREATE TABLE VALIDATION_RULE_EXAMPLE_FIELD (
  ID                         BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  VALIDATION_RULE_EXAMPLE_ID BIGINT,
  NAME                       VARCHAR(255),
  VALUE                      VARCHAR(500),
  CONSTRAINT V_R_EXMPL_FIELD_V_R_EXMPL_FK FOREIGN KEY (VALIDATION_RULE_EXAMPLE_ID) REFERENCES VALIDATION_RULE_EXAMPLE (ID)
);
