CREATE OR REPLACE FUNCTION seticolnumrows ()
  RETURNS integer
  LANGUAGE 'plpgsql'
AS $$
DECLARE
    mark refcursor;
    thisid integer;
    thisdb varchar;
    thisfieldname varchar;
    thistableref  VARCHAR;
    thisschemaref varchar;
    lasttableref VARCHAR := '';
    rec RECORD;
    nrows integer := 0;
    nvals integer := 0;
    metric double precision := 0.0;
    nproc integer :=0;
    -- Threshold for isnominal e.g NOT safe for smaller datasets i.e. < 2000
    nominalthrsh double precision := 3.661; -- for NSHD crudely >= 20 uvs per case
    -- There is a fair bit of experiment needed to tune nominalthrsh
BEGIN
  -- Creates longitudinal style index table with metadata on type of each field indexed
  drop table if exists public.icol;
  RAISE NOTICE 'Deleted old icol table';
  create table public.icol (id serial,
	database varchar(128),
	schemaref varchar(128),
	tableref varchar(128),
	fieldname varchar(128), 
	numrows integer,
	ndiscvals integer,  -- NEW
	class_metric double precision, -- NEW
	isnominal boolean,   -- NEW
	PRIMARY KEY (id)
  );
  insert into public.icol (database,schemaref,tableref,fieldname)
	select
	  cast(table_catalog as varchar),
	  cast(table_schema as varchar),
	  cast(table_name as varchar),
	  cast(column_name as varchar)
    from information_schema.columns where table_catalog = 'nshd';
  -- Remove junk      
  delete from public.icol where schemaref = 'information_schema' or schemaref = 'pg_catalog' or schemaref = 'public';
  EXECUTE 'select count(*) from public.icol' INTO nrows;
  RAISE NOTICE 'There are % fields in icol', nrows;
  -- Step through each table listed in icol and update the number of rows column
  OPEN mark FOR EXECUTE 'SELECT id, database, schemaref, fieldname, tableref FROM public.icol ' ;
  <<process_loop>>
  LOOP
	  FETCH FROM mark INTO rec;
	  IF NOT FOUND THEN
	     EXIT process_loop;
	  END IF;
	  nproc := nproc + 1;
	  -- Pull out the fields for use from the record variable; Not really necessary
	  thisid  := rec.id;
	  thisdb  := rec.database;
	  thisschemaref := rec.schemaref;
	  thisfieldname := rec.fieldname;
	  thistableref := rec.tableref;
	  -- use EXECUTE command INTO to capture dynamic SQL output
	  EXECUTE 'select count(*) from ' || thisschemaref || '.' || thistableref
	      INTO nrows;
	  EXECUTE 'select get_ndiscvals(pschema => $1, ptable => $2, pfield => $3) '
	      INTO nvals
	      USING thisschemaref, thistableref, thisfieldname;
	  EXECUTE 'select SQRT($1) / $2 '
	      INTO metric
	      USING nrows :: integer, nvals :: integer;
	  -- select thisid as "Table Id", nrows as "Number of rows"
	  EXECUTE 'update public.icol set numrows = $1, ndiscvals= $2, class_metric= $3 where id = $4'
	      USING  nrows :: integer, nvals :: integer, metric :: double precision, thisid;
	  -- Guess whether nominal vs continuous on basis of class_metric i.e. smaller is continuous  
	  EXECUTE 'update public.icol set isnominal = CASE WHEN $1 > $2 THEN TRUE ELSE FALSE END where id = $3'
	      USING metric, nominalthrsh, thisid;
  END LOOP process_loop;
  CLOSE mark;
  -- Update isnominal from isnominal_overrides but only if table exists AND has some data in it
  IF EXISTS (
    SELECT FROM 
        pg_tables
    WHERE 
        schemaname = 'public' AND 
        tablename  = 'isnominal_override'
    ) THEN
	  -- Finally override isnominal for specific variables where metric doesn't work
	  UPDATE public.icol AS t1 
	  SET isnominal = t2.isnominal
	  FROM  public.isnominal_override AS t2 
	  WHERE t1.fieldname = t2.fieldname AND t1.schemaref = t2.schemaref AND t1.tableref = t2.tableref;
  END IF;
  RETURN nproc;
 END; 
$$
