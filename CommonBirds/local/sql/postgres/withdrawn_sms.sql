-- Phil Curran May 2024
-- This SQL file creates the withdrawn_sms i.e. withdrawn study members table
-- Do not put entries into this table unless you know what the consequences are!!!
drop table if exists public.withdrawn_sms;
create table public.withdrawn_sms(id serial,
 	serno bigint NOT NULL,
	ntag1 bigint NOT NULL, -- Note adding this is an extra step to force confidence in what is about to happen!
	fromdate date,
	backupfile text, -- File path to backup previous to above date
   	PRIMARY KEY (id)
);
