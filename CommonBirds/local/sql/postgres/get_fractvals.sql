-- Phil Curran December 2024
--
-- Postgresql function to return the results of a query generating
-- a list of fractional values that occur in a specific field
-- Produces numeric value and counts. Meant for detecting isnominal problems. 
CREATE OR REPLACE FUNCTION public.get_fractvals(schemaname text, tablename text, colname text)
 RETURNS TABLE(schm text, tabl text, fld text, val real)
 LANGUAGE plpgsql
AS $function1$
DECLARE
  altname text := colname;

BEGIN
-- Watch out for using ''' to inject an escaped single quote as part of a string
RETURN query
EXECUTE 'select distinct '''
	|| schemaname
	|| ''' schm, '''
	|| tablename
	|| ''' tabl, '''
        || altname
        || ''' fld, '
	|| quote_ident(colname) || '::real val from '
        || quote_ident(schemaname) || '.' || quote_ident(tablename)
        || ' where ' || quote_ident(colname) || ' != ' ||  quote_ident(colname) || '::integer ' ;

END;
$function1$
;
