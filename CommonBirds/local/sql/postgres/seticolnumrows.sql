-- WARNING THIS VERSION IS SUPERCEDED BY THAT IN buildicol.sql
-- THIS VERSION NOT FOR CURRENT USE
-- MAY 2024
CREATE OR REPLACE FUNCTION seticolnumrows ()
  RETURNS integer
  LANGUAGE 'plpgsql'
  STRICT
AS $$
DECLARE
    mark refcursor;
    thisid integer;
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
    nrows integer := 0;
    nproc integer :=0;
BEGIN
  drop table if exists public.icol;
  create table public.icol (id serial,
       	     		host varchar(128),
       	     		database varchar(128),
       	     		schemaref varchar(128),
			       	tableref varchar(128),
			       	numrows integer,
			       	fieldname varchar(128), -- this is the new column
			       	PRIMARY KEY (id)
  );
  insert into public.icol (host,database,schemaref,tableref,fieldname)
       select
	      'localhost',
	      cast(table_catalog as varchar),
	      cast(table_schema as varchar),
	      cast(table_name as varchar),
       	  cast(column_name as varchar)
       from information_schema.columns where table_catalog = 'nshd';
       
  delete from public.icol where schemaref = 'information_schema' or schemaref = 'pg_catalog' or schemaref = 'public';
  -- Step through each table listed in icol and update the number of rows column
  OPEN mark FOR EXECUTE 'SELECT id, database, schemaref, fieldname, host, tableref FROM public.icol ' ;
  <<process_loop>>
  LOOP
  FETCH FROM mark INTO rec;
  IF NOT FOUND THEN
     EXIT process_loop;
  END IF;
  nproc := nproc + 1;
  -- Pull out the fields for use from the record variable
  thisid  := rec.id;
  thisdb  := rec.database;
  thisschemaref := rec.schemaref;
  thisfieldname := rec.fieldname;
  thishost := rec.host;
  thistableref := rec.tableref;
  -- use EXECUTE command INTO to capture dynamic SQL output
  EXECUTE 'select count(*) from ' || thisschemaref || '.' || thistableref INTO nrows;
  -- select thisid as "Table Id", nrows as "Number of rows";
  EXECUTE 'update public.icol set numrows = $1 where id = $2' USING  nrows :: integer,thisid;
  END LOOP process_loop;
  CLOSE mark;
  RETURN nproc;
  END; 
$$
