delimiter $$
drop procedure if exists fixfieldnamess$$
create procedure fixfieldnamess(IN tab varchar(128), IN fld varchar(128))
comment 'Procedure to go through every value in the fld column of table tab and replace dots with underscores'
begin
set @st=concat('update ',tab,' set ',fld,' = REGEXP_REPLACE(',fld,',"[^A-Z,a-z,0-9,_]","_") where ',fld,' REGEXP "[^A-Z,a-z,0-9,_]+"');
prepare stmt from @st;
execute stmt;
deallocate prepare stmt;
end$$

delimiter ;
