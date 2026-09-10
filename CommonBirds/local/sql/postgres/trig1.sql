-- Explore the use of triggers in the gene_trig database
-- Postgresql Special Trigger Variables
--
-- TG_OP
--
--    Data type text; a string of INSERT, UPDATE, DELETE, or TRUNCATE telling for which operation the trigger was fired.
-- TG_RELID
-- 
--     Data type oid; the object ID of the table that caused the trigger invocation.
-- TG_RELNAME
-- 
--     Data type name; the name of the table that caused the trigger invocation. This is now deprecated, and could disappear in a future release. Use TG_TABLE_NAME instead.
-- TG_TABLE_NAME
-- 
--     Data type name; the name of the table that caused the trigger invocation.
-- TG_TABLE_SCHEMA
-- 
--     Data type name; the name of the schema of the table that caused the trigger invocation.

DROP TABLE IF EXISTS ge2019.er_2019_28_audit;


CREATE TABLE ge2019.er_2019_28_audit(
    LIKE ge2019.er_2019_28 including all,
    operation         char(1)   NOT NULL,
    stamp             timestamp NOT NULL,
) ;

CREATE OR REPLACE FUNCTION ge2019.audit_process() RETURNS TRIGGER AS $theaudit$
    BEGIN
        --
        -- If operation is INSERT or DELETE then create a row in recaudit table
	-- to store the new/old record,
        -- If operation is UPDATE then create an entry in the audit table
	-- storing the old and new values of the variable being updated.
	-- Use the special variable TG_OP to work out the operation and
	-- the OLD.* and NEW.* constructs to access values
        --
        IF (TG_OP = 'DELETE') THEN
            INSERT INTO ge2019.er_2019_28_audit SELECT OLD.*, 'D', now();
        ELSIF (TG_OP = 'UPDATE') THEN
            INSERT INTO ge2019.er_2019_28_audit SELECT OLD.*, 'U', now();
        ELSIF (TG_OP = 'INSERT') THEN
            INSERT INTO ge2019.er_2019_28_audit SELECT OLD.*, 'I', now();
        END IF;
        RETURN NULL; -- result is ignored since this is an AFTER trigger
    END;
$theaudit$ LANGUAGE plpgsql;
-- Add the trigger on the table 
DROP TRIGGER IF EXISTS er_2019_28_history ON ge2019.er_2019_28;

CREATE TRIGGER er_2019_28_history
    AFTER UPDATE OR INSERT OR DELETE
    ON ge2019.er_2019_28
    FOR EACH ROW
    EXECUTE FUNCTION ge2019.audit_process ()
/* Now all DDL that modifies the table will be recorded in one of two tables:
  - All INSERT or DELETE DDL statements in <table>_recaudit,
  - All UPDATE statements in <table>_audit
 Why the demarcation?
 Because to recover the table to a point in time requires the following procedure:
 	 1. Get all the inserts or deletes going back to the point in time and:
	    Delete every record inserted in reverse order
	    Insert every record deleted in reverse order
	 2. Get all the updates in reverse order going back to the point in 
	    time and:
	    Delete the record corresponding to the NEW record e.g.
 
	    delete from ge2019.er_2019_28 
	    where cid in 
	    (select cid from ge2019.er_2019_28_audit where operation = 'U');

	    Insert the OLD record into original table e.g.
	    insert 




*/
