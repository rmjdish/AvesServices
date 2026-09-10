-- Phil Curran January 2024
--
-- Postgresql function to return the results of a query generating
-- metadata stripped from the public.icol table for a specific CardNumber/table
-- Returns a table (as set of tuples in postgresql)
-- 
CREATE OR REPLACE FUNCTION public.get_meta_icol(schemaname text, tablename text)
 RETURNS TABLE(schemaref character varying,
 	       tableref character varying,
	       fieldname character varying,
	       numrows integer,
	       ndiscvals integer,
	       class_metric double precision,
	       isnominal boolean)
 LANGUAGE plpgsql
AS $function1$
BEGIN
-- Watch out for using ''' to inject an escaped single quote as part of a string
RETURN query
EXECUTE 'select schemaref, tableref, fieldname, numrows, ndiscvals, class_metric, isnominal'
        || ' from public.icol where schemaref = '
	|| ' ''' || quote_ident(schemaname) || ''' '
	|| ' and tableref = '
	|| ' ''' || quote_ident(tablename) || ''' ';
END;
$function1$
;
