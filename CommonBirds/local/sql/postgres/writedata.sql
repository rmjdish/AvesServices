-- FUNCTION: nshd.public.writedata(character varying, character varying)

-- DROP FUNCTION nshd.public.writedata(character varying, character varying);

CREATE OR REPLACE FUNCTION nshd.public.writedata(
	tblname character varying,
	filename character varying)
    RETURNS void
    LANGUAGE 'plpgsql'

    COST 100
    VOLATILE 
AS $BODY$
DECLARE
    numrows integer := 0;
    thequery varchar;
BEGIN
SELECT extractData(tblname) INTO thequery;
EXECUTE 'COPY  (' || thequery || ') TO ''' || filename || ''' WITH CSV HEADER';
RAISE NOTICE 'writeData : %',thequery;
END; 
$BODY$;

ALTER FUNCTION nshd.public.writedata(character varying, character varying)
    OWNER TO root;
