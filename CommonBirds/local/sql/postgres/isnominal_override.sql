 drop table if exists public.isnominal_override;
 create table public.isnominal_override(id serial,
 	database varchar(128),
	schemaref varchar(128),
	tableref varchar(128),
   	fieldname varchar(128), 
	isnominal boolean,   -- NEW
   	PRIMARY KEY (id)
  );
