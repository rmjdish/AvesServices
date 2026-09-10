-- Phil Curran 22 April 2024
--
-- In the following script I use the name icol for an index table
-- of all variables in all schemas in all tables within the database.
-- Your index table may well have a more sensible name!
--
-- This routine goes through every table in the icol index and changes fieldnames to lowercase
BEGIN ;

CREATE OR REPLACE FUNCTION public.fieldnames_2_lowercase()
 RETURNS void
 LANGUAGE plpgsql
AS $function$
DECLARE
	rec RECORD;
	numvars INT;
BEGIN
  select seticolnumrows() INTO numvars;
  for rec in SELECT distinct schemaref, tableref FROM public.icol ORDER BY schemaref, tableref
    loop
	-- PG ver 12 is stricter on types so needs the casts in the select/perform below
	perform lowercase_table(rec.schemaref::VARCHAR(256), rec.tableref::VARCHAR(256));
	RAISE NOTICE 'Completed Schema % . Table %',rec.schemaref, rec.tableref;
    end loop;
	-- Do it again now we've altered fieldnames
    select seticolnumrows() INTO numvars;
END;  
$function$ ;


-- The following function works on a single table working through all columns to lowercase fieldnames
CREATE OR REPLACE FUNCTION public.lowercase_table(schname character varying, tabname character varying)
 RETURNS void
 LANGUAGE plpgsql
AS $function2$
declare this RECORD;
BEGIN
  for this in SELECT distinct fieldname FROM public.icol WHERE schemaref = schname AND tableref = tabname
    loop
    if lower(this.fieldname) not in ('ntag1', 'serno') AND 
       md5(this.fieldname) <> md5(lower(this.fieldname)) THEN
       EXECUTE format('ALTER TABLE %I.%I RENAME COLUMN %I TO %I',
       schname,tabname,this.fieldname,lower(this.fieldname)); 
    end if;
    end loop;
END;
$function2$;
COMMIT;
