-- Phil Curran November 2023
--
-- There have been some problematic coding schemes used for
-- NSHD categorical 
-- This  file will create that table for you

CREATE OR REPLACE PROCEDURE findbadcoding ()
  LANGUAGE 'plpgsql'
AS $function1$
DECLARE
    mark refcursor;
    thistable  VARCHAR;
    thisschema varchar;
    thiscolumn varchar;
    thisdtype varchar;
    thisprobs integer;
    rec RECORD;
    nproc bigint :=0;
BEGIN
  -------------------- CREATE TABLES --------------------------------------
  -- Get rid of old problem information, need to drop first
  DROP TABLE IF EXISTS public.problem_nominals;
  -- Create Table for data type info
  RAISE NOTICE 'Building public.problem_nominals table.';
  CREATE TABLE  public.problem_nominals (id bigserial,
  	 schemaref varchar(128),
	 tableref varchar(128),
	 fieldname varchar(128), 
	 fieldvalue real,
	 PRIMARY KEY (id)
  );
  -- Make sure serial id gets reset to 0
  TRUNCATE TABLE public.problem_nominals RESTART IDENTITY;
  RAISE NOTICE 'Populating problem_nominals table';
  ----------------------- Finished Creating Tables -------------------------
  -- Now loop though generating the frequencies
  for rec in SELECT DISTINCT table_schema, table_name, column_name, data_type
             FROM public.icol_types WHERE isnominal
	     ORDER BY table_schema, table_name
    LOOP
      nproc := nproc + 1;
      thisprobs := 0;
      -- Pull out the fields for use from the record variable; Not really necessary
      thisschema := rec.table_schema;
      thistable  := rec.table_name;
      thiscolumn    := rec.column_name;
      thisdtype     := rec.data_type;
      -- Screen for schemas that contain data where frequencies don't make sense
      CONTINUE WHEN thisschema IN ('omics');  -- Skip these schemas
      CONTINUE WHEN thistable  IN ('metabmlcs09_org_1','metabmlcs09_org_2'); -- Skip these tables
      CONTINUE WHEN thisdtype IN ('character varying', 'text', 'date', 'character', 'time without time zone', 'timestamp with time zone'); -- Don't bother with strings
      SELECT COUNT(*) INTO thisprobs FROM get_fractvals(thisschema, thistable, thiscolumn);
      IF thisprobs >0 THEN
      	 INSERT INTO public.problem_nominals (schemaref, tableref, fieldname, fieldvalue)
      	     SELECT distinct * from get_fractvals(thisschema, thistable, thiscolumn);
      END IF;
    END LOOP;
  RAISE NOTICE 'Processed variables in % variables and populated the public.problem_nominals table.',nproc;
END; 
$function1$ ;


