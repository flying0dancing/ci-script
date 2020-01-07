ALTER TABLE DATASET_SCHEMA
  ADD PRODUCT_ID BIGINT;

UPDATE DATASET_SCHEMA DS1 LEFT JOIN (SELECT P.ID as P_ID, DS2.ID as DS_ID
    FROM DATASET_SCHEMA DS2
    join PRODUCT_CONFIG_DATASET_SCHEMA PDS on DS2.ID = PDS.DATASET_SCHEMA_ID
    join PRODUCT_CONFIG P on P.ID = PDS.PRODUCT_CONFIG_ID) AS PJ
    ON DS1.ID = PJ.DS_ID
SET DS1.PRODUCT_ID = PJ.P_ID;

ALTER TABLE DATASET_SCHEMA ADD CONSTRAINT DS_PRODUCT_FK
FOREIGN KEY (PRODUCT_ID) REFERENCES PRODUCT_CONFIG (ID);