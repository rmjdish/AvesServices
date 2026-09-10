-- Phil Curran November 2023
--
-- In the following plpgsql routines I use the name icol for an index table
-- of all variables in all schemas in all tables within the database.
-- Your index table may well have a more sensible name!
--
-- In order to run the following code you need to create a table called icol_types like so:
-- create table icol_types as select 
-- table_schema, table_name, column_name, data_type
-- from information_schema.columns
-- where table_name in (select distinct tableref from icol);
-- This  file will create that table for you

CREATE OR REPLACE PROCEDURE buildfreqs ()
  LANGUAGE 'plpgsql'
AS $function1$
DECLARE
    mark refcursor;
    thistableref  VARCHAR;
    thisschemaref varchar;
    rec RECORD;
    nproc bigint :=0;
BEGIN
  -------------------- CREATE TABLES --------------------------------------
  -- Get rid of old type information, need to drop view first
  DROP VIEW IF EXISTS public.icol_frequencies;
  DROP TABLE IF EXISTS public.icol_types;
  -- Create Table for data type info
  RAISE NOTICE 'Building public.icol_types table.';
  CREATE TABLE public.icol_types AS SELECT table_schema, table_name, column_name, data_type
  FROM information_schema.columns
  WHERE table_name IN (SELECT DISTINCT tableref FROM public.icol);
  -- Clean up copies of tables left in public schema
  DELETE FROM public.icol_types WHERE table_schema = 'public';
  -- Push isnominal into icol_types table from icol
  ALTER TABLE public.icol_types
  	ADD COLUMN id bigserial,
  	ADD COLUMN isnominal boolean;
  -- Update isnominal from icol
  UPDATE public.icol_types t1
  	 SET isnominal = (SELECT t2.isnominal FROM public.icol t2
	 WHERE t1.table_schema = t2.schemaref AND
	       t1.table_name = t2.tableref AND
	       t1.column_name = t2.fieldname order by t2.id);
  RAISE NOTICE 'Created public.icol_types';
  -- Creates longitudinal style index table with metadata on type of each field indexed
  /* Skip this to avoid id overflow */
  drop table if exists public.frequencies;
  create table public.frequencies (id bigserial,
		       fieldname varchar(128),
			   fieldvalue bigint,
			   fieldcount integer,
			   numdecplcs integer,
		       	PRIMARY KEY (id)
  );
  -- Make sure serial id gets reset to 0
  TRUNCATE TABLE public.frequencies RESTART IDENTITY;
  RAISE NOTICE 'Populating frequencies table';
  ----------------------- Finished Creating Tables -------------------------
  -- Now loop though generating the frequencies
  for rec in SELECT DISTINCT schemaref, tableref FROM public.icol ORDER BY schemaref, tableref
    LOOP
      nproc := nproc + 1;
      -- Pull out the fields for use from the record variable; Not really necessary
      thisschemaref := rec.schemaref;
      thistableref := rec.tableref;
      -- Screen for schemas that contain data where frequencies don't make sense
      CONTINUE WHEN thisschemaref IN ('omics');  -- Skip these schemas
      -- CONTINUE WHEN thistableref  IN ('metabmlcs09_org_1','metabmlcs09_org_2'); -- Skip these tables
      CALL frequency_table(thisschemaref::text, thistableref::text);
    END LOOP;
  -- Recreate view icol_frequencies
  CALL buildifview();
  -- Copy frequencies table to CSV file located in /tmp
  CALL table2csv('icol_frequencies', '/tmp/icol_frequencies.csv');
  CALL table2csv('frequencies', '/tmp/frequencies.csv');
  CALL table2csv('icol_types', '/tmp/icol_types.csv');
  RAISE NOTICE 'Processed variables in % tables and copied generated CSV files to /tmp.',nproc;
END; 
$function1$ ;


-- The following function works on a single table working through all columns to generate frequencies
CREATE OR REPLACE PROCEDURE public.frequency_table(schname text, tabname text)
 LANGUAGE plpgsql
AS $function2$
DECLARE
this RECORD;
nproc bigint := 0;
nvars bigint := 0;
BEGIN
RAISE NOTICE 'Working on %.%',schname, tabname;
for this in select column_name, data_type, isnominal from public.icol_types where table_schema = schname and table_name = tabname
  loop
    nvars := nvars + 1;
    -- if not an identifier and isnominal variable
    if lower(this.column_name) not in ('ntag1', 'serno', 'sex', 'inf') and this.isnominal then
       CASE this.data_type
         WHEN 'bigint', 'integer', 'smallint' THEN
	     nproc := nproc + 1;
             EXECUTE 'insert into public.frequencies (fieldname, fieldvalue, fieldcount, numdecplcs) select * from dofreqs($1, $2, $3)'
             USING schname, tabname, this.column_name;
	     WHEN 'double precision', 'numeric' THEN
	     -- Multiply all the dp values by 100 and set numdecplcs = 2
	     nproc := nproc +1;
	     EXECUTE 'insert into public.frequencies (fieldname, fieldvalue, fieldcount, numdecplcs) select * from dofreqsdp($1, $2, $3)'
             USING schname, tabname, this.column_name;
	     ELSE
	       CONTINUE;
       END CASE;
       COMMIT;
    end if;
  end loop;
  -- Clean up likely missing values -- Maybe not commented out for parity
  -- EXECUTE 'delete from public.frequencies where fieldvalue > 7000';
  RAISE NOTICE 'In %.% there were % variables, of which frequencies produced for %',
          schname, tabname, nvars, nproc;
END;
$function2$;

