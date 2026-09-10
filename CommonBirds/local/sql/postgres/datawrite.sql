CREATE OR REPLACE FUNCTION nshd.public.datawrite(_tblname character varying, _filename character varying)
 RETURNS void
 LANGUAGE plpgsql
AS $function$
DECLARE
    numrows integer := 0;
    thequery varchar := '';
BEGIN
-- Old version EXECUTE 'SELECT dataExtract(quote_ident(''' || _tblname  || '''))' INTO thequery;
EXECUTE FORMAT('SELECT dataextract(quote_ident(''%s''))',_tblname) INTO thequery;
-- Old version EXECUTE 'COPY  (' || thequery || ') TO  ''' || _filename || ''' WITH CSV HEADER';
EXECUTE FORMAT('COPY (%s) TO ''%s'' WITH CSV HEADER',thequery,_filename);
RAISE NOTICE 'datawrite : %',thequery;
END;
$function$
