-- MySQL function to return the number of "items"(=vars) in a given
-- category, from the category members table, avor zero if category id not found
delimiter $$
drop function if exists cat_var_count$$
create function cat_var_count(acode int(10) unsigned)
returns int(10)
reads sql data
deterministic
begin 
	declare nitems int(10) default 0;
	select count(*) as "items"
		into nitems
		from categorymembers 
		where code=acode;
	return nitems;
end $$
delimiter ;
