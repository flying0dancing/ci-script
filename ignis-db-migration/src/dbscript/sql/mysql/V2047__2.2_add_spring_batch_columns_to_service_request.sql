ALTER TABLE SVC_SERVICE_REQUEST ADD START_TIME DATETIME;
ALTER TABLE SVC_SERVICE_REQUEST ADD END_TIME DATETIME;
ALTER TABLE SVC_SERVICE_REQUEST ADD STATUS VARCHAR(10);
ALTER TABLE SVC_SERVICE_REQUEST ADD EXIT_CODE VARCHAR(10);

UPDATE SVC_SERVICE_REQUEST SR
LEFT JOIN (
    SELECT JOB_EXECUTION_ID, START_TIME, END_TIME, STATUS, EXIT_CODE
    FROM BATCH_JOB_EXECUTION
) BJE
ON SR.JOB_EXECUTION_ID = BJE.JOB_EXECUTION_ID
SET
    SR.START_TIME = BJE.START_TIME,
    SR.END_TIME = BJE.END_TIME,
    SR.STATUS = BJE.STATUS,
    SR.EXIT_CODE = BJE.EXIT_CODE;