delimiter $$
drop procedure if exists delete_descriptives_entry$$
create procedure delete_descriptives_entry(IN fld varchar(128))
comment 'Procedure to remove a single entry for the field with name fld in descriptives table'
begin
	delete from descriptives where name = fld;
end$$

delimiter ;
