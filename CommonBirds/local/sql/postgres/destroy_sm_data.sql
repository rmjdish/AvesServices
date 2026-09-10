-- Phil Curran 30 March 2022
--
-- In the following script I use the name icol for an index table
-- of all variables in all schemas in all tables within the database.
-- Your index table may well have a more sensible name!
--
-- In order to run the following code you need to create a table called icol_types like so:
-- create table icol_types as select 
-- table_schema, table_name, column_name, data_type
-- from information_schema.columns
-- where table_name in (select distinct tableref from icol);
-- This  file will create that table for you
-- 
-- BEWARE!! This SQL function will destroy a SM's data in every table by overwriting
-- all values with -9 and dates with the year 9999
-- BEWARE!! This SQL function will destroy data for every SM in the withdrawn_sms table.
-- It uses the destroy_sm_data function below.
-- This function takes no arguments - it reads all entries from the withdrawn_sms table
-- Do not run this unless you know exactly what it will do!

CREATE OR REPLACE FUNCTION public.destroy_all_withdrawn_sms()
 RETURNS void
 LANGUAGE plpgsql
 AS $func$
 DECLARE
 	rec RECORD;
 BEGIN
	DROP TABLE IF EXISTS public.icol_types;
	-- Create Table for data type info
	RAISE NOTICE 'Building public.icol_types table.';
	CREATE TABLE public.icol_types AS SELECT table_schema, table_name, column_name, data_type
	FROM information_schema.columns
	WHERE table_name IN (SELECT DISTINCT tableref FROM public.icol);

 	for rec in select serno,ntag1 from public.withdrawn_sms
    	loop
    	perform public.destroy_sm_data(rec.serno::integer);
    	RAISE NOTICE 'Data Destroyed for SERNO: % NTAG: %', rec.serno, rec.ntag1;
    	end loop;

 END;
 $func$ ;

-- BEWARE!! This SQL function will destroy a SM's data in every table by overwriting
-- all values with -9 and dates with the year 9999
-- This function takes one argument - the SM's identifier (SERNO) as an integer
-- Do not run this unless you know exactly what it will do!
CREATE OR REPLACE FUNCTION public.destroy_sm_data(serno integer)
 RETURNS void
 LANGUAGE plpgsql
AS $function$
DECLARE
	rec RECORD;
BEGIN
  for rec in SELECT distinct table_schema, table_name FROM public.icol_types order by table_schema, table_name
      loop
	-- PG ver 12 is stricter on types so needs the casts in the select/perform below
	perform destroy_sm_table(rec.table_schema::VARCHAR(256), rec.table_name::VARCHAR(256), serno);
	RAISE NOTICE 'Completed Table %',rec.table_name;
      end loop;
  END;  
$function$ ;


-- The following function works on a single table working through all columns to set to -9 like values
CREATE OR REPLACE FUNCTION public.destroy_sm_table(schname character varying, tabname character varying, id integer)
 RETURNS void
 LANGUAGE plpgsql
AS $function2$
declare this RECORD;
BEGIN
for this in select column_name, data_type from public.icol_types where table_schema = schname and table_name = tabname
    loop
    if lower(this.column_name) not in ('ntag1', 'ntag2', 'ntag3','ntag4','ntag5',
    								   'ntag6','ntag7','ntag8','ntag9','serno') then
       CASE this.data_type
         WHEN 'bigint' THEN
	     EXECUTE 'update '|| schname || '.' || tabname ||
       	      ' set '   || quote_ident(this.column_name) || ' = -9 where serno = ' || id ;
	 WHEN 'character', 'character varying', 'text' THEN
       	     EXECUTE 'update '|| schname || '.' || tabname ||
              ' set '   || quote_ident(this.column_name) || ' = ' || quote_literal('-9') || ' where serno = ' || id ;
	 WHEN 'date' THEN
       	     EXECUTE 'update '|| schname || '.' || tabname ||
              ' set '   || quote_ident(this.column_name) || ' = ' || quote_literal('9999-01-01') || ' where serno = ' || id ;
	 WHEN 'double precision' THEN
       	     EXECUTE 'update '|| schname || '.' || tabname ||
              ' set '   || quote_ident(this.column_name) || ' = -9.0 where serno = ' || id ;
	 WHEN 'integer', 'smallint' THEN
       	     EXECUTE 'update '|| schname || '.' || tabname ||
              ' set '   || quote_ident(this.column_name) || ' = -9 where serno = ' || id ;
	 WHEN 'time without time zone', 'timestamp with time zone' THEN
      	     EXECUTE 'update '|| schname || '.' || tabname ||
              ' set '   || quote_ident(this.column_name) || ' = NOW() where serno = ' || id ;
	 ELSE
	     RAISE NOTICE 'Unknown Datatype(%)', dattype;
       END CASE;
    end if;
    end loop;
END;
$function2$;
COMMIT;
