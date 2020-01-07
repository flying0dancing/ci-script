DECLARE @startDateConstraint nvarchar(123);

select @startDateConstraint = cname from
        (select name as cname, col_name(parent_object_id, parent_column_id) as c from sys.default_constraints
         where name like 'DF__%' and object_id('VALIDATION_RULE') = parent_object_id)
  AS CONSTRAINTS where CONSTRAINTS.c = 'START_DATE';

IF @startDateConstraint IS NOT NULL
  EXECUTE('ALTER TABLE VALIDATION_RULE DROP CONSTRAINT [' + @startDateConstraint + ']');



DECLARE @endDateConstraint nvarchar(123);

select @endDateConstraint = cname from
        (select name as cname, col_name(parent_object_id, parent_column_id) as c from sys.default_constraints
         where name like 'DF__%' and object_id('VALIDATION_RULE') = parent_object_id)
  AS CONSTRAINTS where CONSTRAINTS.c = 'END_DATE';

IF @endDateConstraint IS NOT NULL
  EXECUTE('ALTER TABLE VALIDATION_RULE DROP CONSTRAINT [' + @endDateConstraint + ']');

alter table VALIDATION_RULE
  drop constraint UNIQUE_RULE
go


ALTER TABLE VALIDATION_RULE ALTER COLUMN START_DATE date null;
ALTER TABLE VALIDATION_RULE ALTER COLUMN END_DATE date null;

GO

alter table VALIDATION_RULE
  add constraint UNIQUE_RULE
unique (DATASET_SCHEMA_ID, RULE_ID, VERSION, START_DATE, END_DATE)
go
