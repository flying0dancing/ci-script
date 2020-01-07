drop user IGNIS$OWNER cascade;
drop user IGNIS$TEST cascade;
drop user DESIGNSTUDIO cascade;
drop role IGNIS$RW_ROLE;
drop role IGNIS$R_ROLE;
drop role DESIGNSTUDIO$RW_ROLE;
drop role DESIGNSTUDIO$R_ROLE;

-- Ignis Server
CREATE USER IGNIS$OWNER IDENTIFIED BY password
  DEFAULT TABLESPACE users
  TEMPORARY TABLESPACE temp
  QUOTA UNLIMITED ON users;

grant create session to IGNIS$OWNER;
grant create table to IGNIS$OWNER;
grant create sequence to IGNIS$OWNER;
grant create view to IGNIS$OWNER;
grant create trigger to IGNIS$OWNER;
grant create procedure to IGNIS$OWNER;
grant select on SYS.pending_trans$ to IGNIS$OWNER;
grant select on SYS.dba_2pc_pending to IGNIS$OWNER;
grant select on SYS.dba_pending_transactions to IGNIS$OWNER;
grant execute on SYS.dbms_xa to IGNIS$OWNER;

CREATE ROLE IGNIS$RW_ROLE;
CREATE ROLE IGNIS$R_ROLE;

-- Ignis server integration tests
CREATE USER IGNIS$TEST IDENTIFIED BY password
  DEFAULT TABLESPACE users
  TEMPORARY TABLESPACE temp
  QUOTA UNLIMITED ON users;

grant create session to IGNIS$TEST;
grant create table to IGNIS$TEST;
grant create sequence to IGNIS$TEST;
grant create view to IGNIS$TEST;
grant create trigger to IGNIS$TEST;
grant create procedure to IGNIS$TEST;
grant select on SYS.pending_trans$ to IGNIS$TEST;
grant select on SYS.dba_2pc_pending to IGNIS$TEST;
grant select on SYS.dba_pending_transactions to IGNIS$TEST;
grant execute on SYS.dbms_xa to IGNIS$TEST;

-- Design Studio
CREATE USER DESIGNSTUDIO IDENTIFIED BY password
  DEFAULT TABLESPACE users
  TEMPORARY TABLESPACE temp
  QUOTA UNLIMITED ON users;

grant create session to DESIGNSTUDIO;
grant create table to DESIGNSTUDIO;
grant create sequence to DESIGNSTUDIO;
grant create view to DESIGNSTUDIO;
grant create trigger to DESIGNSTUDIO;
grant create procedure to DESIGNSTUDIO;
grant select on SYS.pending_trans$ to DESIGNSTUDIO;
grant select on SYS.dba_2pc_pending to DESIGNSTUDIO;
grant select on SYS.dba_pending_transactions to DESIGNSTUDIO;
grant execute on SYS.dbms_xa to DESIGNSTUDIO;

CREATE ROLE DESIGNSTUDIO$RW_ROLE;
CREATE ROLE DESIGNSTUDIO$R_ROLE;