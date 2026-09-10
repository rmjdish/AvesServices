-- Phil Curran November 2023
--
-- Postgresql function to return the results of a query generating
-- tuples of (variable name, value, count, numdecplaces) for frequency counts
-- Produces integer value and counts (but multiplies floats by 100 and sets ndec = 2 type columns)
CREATE OR REPLACE FUNCTION public.dofreqs(schemaname text, tablename text, colname text)
 RETURNS TABLE(fld text, val bigint, cnt integer, ndec integer)
 LANGUAGE plpgsql
AS $function1$
DECLARE
  altname text := colname;
  
BEGIN
-- Watch out for using ''' to inject an escaped single quote as part of a string
RETURN query
EXECUTE 'select '''
        || altname
        || ''' fld,'
        || quote_ident(colname) || '::bigint val, count(*)::integer cnt, 0::integer ndec from '
        || quote_ident(schemaname) || '.' || quote_ident(tablename)
        || ' group by ' || quote_ident(colname) || ' order by '
        ||  quote_ident(colname);
END;
$function1$
;

CREATE OR REPLACE FUNCTION public.dofreqsdp(schemaname text, tablename text, colname text)
 RETURNS TABLE(fld text, val bigint, cnt integer, ndec integer)
 LANGUAGE plpgsql
AS $function2$
DECLARE
  altname text := colname;
  altschm text := schemaname;
  alttabl text := tablename;
  thisvalue double precision;
BEGIN
-- Watch out for using ''' to inject an escaped single quote as part of a string
EXECUTE 'select ' 
         || quote_ident(altname) || ' from '
         || quote_ident(altschm) 
         ||'.'
         ||quote_ident(alttabl)
         INTO thisvalue ;
RAISE NOTICE 'dofreqsdp: got value of % from %', thisvalue, altname; 
IF thisvalue = floor(thisvalue) THEN
  -- Whole number so treat as integer
  SELECT dofreqs(schemaname, tablename, colname);
ELSE
  -- Real number
  thisvalue := 100 * thisvalue;
RETURN query
EXECUTE 'select '''
        || altname
        || ''' fld, '
        || quote_ident(thisvalue) || '::bigint val, count(*)::integer cnt, 2::integer ndec from '
        || quote_ident(schemaname) || '.' || quote_ident(tablename)
        || ' group by ' || quote_ident(colname) || ' order by '
        ||  quote_ident(colname) ;
END IF;
END;
$function2$
