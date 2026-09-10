delimiter $$
drop function if exists hasvarsec; $$
create function hasvarsec(varname VARCHAR(64)) returns integer reads sql data
begin
	declare secval INTEGER default -1;
	select secLevel into secval from robin.varsecmod where Name = varname;
	return secval;
end; $$

drop function if exists checkauth; $$
create function checkauth(user VARCHAR(20), varname VARCHAR(64)) returns integer 
reads sql data
begin
	declare usersec INTEGER default 0;
	select secLevel into usersec from robin.privileges where username = user;
	if usersec >= hasvarsec(varname) then
	    return 1;
	else 
	    return 0;
	end if;
end; $$

drop function if exists hasvarmsg; $$
create function hasvarmsg(varname VARCHAR(64)) returns varchar(255) reads sql data
begin
	declare varmsg VARCHAR(255) default "Standard security restrictions apply to this variable";
	declare varmid INTEGER default -1;
	select messageId into varmid from robin.varsecmod where Name = varname;
	select mtext into varmsg from robin.messages where messageId = varmid;
	return varmsg;
end; $$

delimiter ;