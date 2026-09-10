CREATE OR REPLACE PROCEDURE nshd.public.table2csv(_tblname text, _filename text)
 LANGUAGE plpgsql
AS $function$
DECLARE
    numrows integer := 0;
    thequery varchar := '';
BEGIN
thequery := 'SELECT * FROM ' || _tblname;
EXECUTE FORMAT('COPY (%s) TO ''%s'' WITH CSV HEADER',thequery,_filename);
RAISE NOTICE 'table2csv : %',thequery;
END;
$function$
