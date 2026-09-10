-- Phil Curran January 2024
--
-- Postgresql function to return the results of a query generating
-- frequency counts for nominal fields in a specific CardNumber/table
-- Returns a table (as set of tuples in postgresql)
-- 
CREATE OR REPLACE FUNCTION public.get_freqs(schemaname text, tablename text)
 RETURNS TABLE(table_schema character varying, table_name character varying, fieldname character varying, fieldvalue bigint, fieldcount integer, data_type character varying, isnominal boolean)
 LANGUAGE plpgsql
AS $function1$
BEGIN
-- Watch out for using ''' to inject an escaped single quote as part of a string
RETURN query
EXECUTE 'select table_schema, table_name, fieldname, fieldvalue, fieldcount, data_type, isnominal'
        || ' from public.icol_frequencies where table_schema = '
	|| ' ''' || quote_ident(schemaname) || ''' '
	|| ' and table_name = '
	|| ' ''' || quote_ident(tablename) || ''' ';
END;
$function1$
;
