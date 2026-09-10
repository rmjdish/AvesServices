-- FUNCTION: nshd.public.dataextract(character varying)
-- DROP FUNCTION nshd.public.dataextract(character varying);
--
CREATE OR REPLACE FUNCTION nshd.public.dataextract(
	tbl character varying)
    RETURNS character varying
    LANGUAGE 'plpgsql'

    COST 100
    VOLATILE 
AS $BODY$
DECLARE
    mark refcursor;
    thisdb text;
    thisfieldname text;
    thistableref  text;
    thisschemaref text;
    lasttableref text := '';
    rec RECORD;
    extjoin text := '';
    extcols text;
    extqry text := '';
    serno text := 'serno'; -- The common identifier used in all indexed tables
    gotserno boolean := FALSE;
    nrows integer := 0;
    
BEGIN
  -- It's important to order this query by tableref as we test to see if this changes and by numrows to ensure the maximum number of results
  OPEN mark FOR EXECUTE
  'SELECT * FROM (SELECT distinct on (fieldname)  database, schemaref, fieldname, tableref, numrows FROM nshd.public.icol ' ||
  'WHERE fieldname IN (SELECT varname FROM nshd.public.' || tbl || ') order by fieldname, numrows DESC) AS foo ORDER BY numrows DESC,tableref';
  <<process_loop>>
  LOOP
  FETCH FROM mark INTO rec;
  IF NOT FOUND THEN
     EXIT process_loop;
  END IF;
  nrows := nrows + 1;
  -- Pull out the fields for use from the record variable
  thisdb  := rec.database;
  thisschemaref := rec.schemaref;
  thisfieldname := rec.fieldname;
  thistableref := rec.tableref;
  -- Check to see if we need to proceed the field name with a comma
  IF nrows <= 1 THEN
     extcols := CONCAT(extcols,thisfieldname);
     IF thisfieldname = serno THEN
        gotserno := TRUE;
     END IF;
  ELSE -- No first field so add comma
    IF gotserno = FALSE THEN
       extcols := CONCAT(extcols,', ',thisfieldname);
       IF thisfieldname = serno THEN
          gotserno := TRUE;
       END IF;
    ELSE -- Not first field but have already found the serno variable name
         IF thisfieldname != serno THEN
      	    extcols := CONCAT(extcols,', ',thisfieldname);
	 END IF; -- Throw away another copy of serno
    END IF; -- Check for serno
  END IF; -- Finished processing field names
  -- Check if the table name has changed from the last one
  IF thistableref != lasttableref THEN
     -- Check to see if you need to proceed the table name with JOIN
     IF nrows > 1 THEN
     	extjoin := CONCAT(extjoin,' NATURAL LEFT JOIN ',thisdb,'.',thisschemaref,'.',thistableref);
     ELSE
        extjoin := CONCAT(extjoin,thisdb,'.',thisschemaref,'.',thistableref);
     END IF; -- 
     lasttableref := thistableref;
  END IF; -- check for JOINs
  END LOOP process_loop;
  CLOSE mark;
  -- put it all together
  extqry = CONCAT('SELECT ',extcols,' FROM ',extjoin);
  -- prepare statmt AS extqry;
  RAISE NOTICE 'dataExtract Query Constructed : %', extqry;
  RETURN extqry;
  END; 
$BODY$;

ALTER FUNCTION nshd.public.dataextract(character varying)
    OWNER TO postgres;
