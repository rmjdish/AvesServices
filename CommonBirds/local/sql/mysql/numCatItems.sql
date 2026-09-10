-- MySQL function to return the number of "items"(=vars) in a given
-- category, or zero if category id not found
delimiter $$
drop function if exists num_cat_items$$
create function num_cat_items(acode int(10) unsigned)
returns int(10)
reads sql data
deterministic
begin 
	declare nitems int(10) default 0;
	select items as "items"
		into nitems
		from categorymembercount 
		where code=acode;
	return nitems;
end $$
delimiter ;
