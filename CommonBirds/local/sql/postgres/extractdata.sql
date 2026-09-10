-- FUNCTION: nshd.public.extractdata(character varying)
-- Takes one parameter tbl, the name of a table with one field: 'varname'
-- tbl has one row for each variable name in a basket

-- DROP FUNCTION nshd.public.extractdata(character varying);

CREATE OR REPLACE FUNCTION nshd.public.extractdata(
	tbl character varying)
    RETURNS character varying
    LANGUAGE 'plpgsql'

    COST 100
    VOLATILE 
AS $BODY$
DECLARE
    mark refcursor;
    thisdb varchar;
    thisfieldname varchar;
    thishost VARCHAR;
    thistableref  VARCHAR;
    thisschemaref varchar;
    lasttableref VARCHAR := '';
    rec RECORD;
    extjoin varchar := '';
    extcols varchar;
    extqry text;
    gotserno boolean := FALSE;
    numrows integer := 0;
    
BEGIN
  -- It's important to order this query by tableref as we test to see if this changes
  OPEN mark FOR EXECUTE 'SELECT database, schemaref, fieldname, host, tableref FROM nshd.public.loci ' ||
	                'WHERE fieldname IN (SELECT varname FROM nshd.public.' || tbl || ') ORDER BY tableref';
  <<process_loop>>
  LOOP
  FETCH FROM mark INTO rec;
  IF NOT FOUND THEN
     EXIT process_loop;
  END IF;
  numrows := numrows + 1;
  -- Pull out the fields for use from the record variable
  thisdb  := rec.database;
  thisschemaref := rec.schemaref;
  thisfieldname := rec.fieldname;
  thishost := rec.host;
  thistableref := rec.tableref;
  -- Check to see if we need to proceed the field name with a comma
  IF gotserno = FALSE AND numrows <= 1 THEN
     extcols := CONCAT(extcols,thisfieldname);
     IF thisfieldname = 'serno' THEN
        gotserno := TRUE;
     END IF;
  ELSE -- No serno yet but not first row so add comma
    IF gotserno = FALSE AND numrows > 1 THEN
       extcols := CONCAT(extcols,', ',thisfieldname);
       IF thisfieldname = 'serno' THEN
          gotserno := TRUE;
       END IF;
    ELSE
      -- Don't duplicate the serno variable name
      IF gotserno = TRUE AND numrows <= 1 THEN
         IF thisfieldname != 'serno' THEN
      	    extcols := CONCAT(extcols,thisfieldname);
	 END IF;
      ELSE
        -- Don't duplicate the serno variable name
        IF gotserno = TRUE AND numrows > 1 THEN
	   IF thisfieldname != 'serno' THEN
	      extcols := CONCAT(extcols,', ',thisfieldname);
	   END IF;
	END IF;
      END IF;
    END IF;
  END IF;

  -- Check if the table name has changed from the last one
  IF thistableref != lasttableref THEN
     -- Check to see if you need to proceed the table name with JOIN
     IF numrows > 1 THEN
        extjoin := CONCAT(extjoin,' NATURAL JOIN ',thisdb,'.',thisschemaref,'.',thistableref);
     ELSE
        extjoin := CONCAT(extjoin,thisdb,'.',thisschemaref,'.',thistableref);
     END IF;
     lasttableref := thistableref;
  END IF;
  END LOOP process_loop;
  CLOSE mark;
  -- put it all together
  extqry = CONCAT('SELECT ',extcols,' FROM ',extjoin);
  -- prepare statmt AS extqry;
  RAISE DEBUG 'extractData Attempting : %', extqry;

  RETURN extqry;
  END; 
$BODY$;

ALTER FUNCTION nshd.public.extractdata(character varying)
    OWNER TO root;
