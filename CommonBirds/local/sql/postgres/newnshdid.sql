-- Phil Curran May 2024
-- Create a version of the newnshdid table in PostgreSQL
drop table if exists public.newnshdid;
create table public.newnshdid(
	serno bigint NOT NULL,
	sex   int NOT NULL,
 	ntag1 bigint NOT NULL,
	ntag2 bigint NOT NULL,
 	ntag3 bigint NOT NULL,
	ntag4 bigint NOT NULL,
 	ntag5 bigint NOT NULL,
	ntag6 bigint NOT NULL,
 	ntag7 bigint NOT NULL,
	ntag8 bigint NOT NULL,
 	ntag9 bigint NOT NULL,
	PRIMARY KEY (serno)
);
