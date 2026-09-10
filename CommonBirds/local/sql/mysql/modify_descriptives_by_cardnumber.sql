delimiter $$
drop procedure if exists modify_descriptives_by_cardnumber $$
create procedure modify_descriptives_by_cardnumber(IN thiscardnum varchar(128), IN newdescrip mediumtext)
comment 'Procedure to modify every entry for variables having CardNumber thiscardnum in descriptives table'
begin
    DECLARE x integer;
    DECLARE mycdn varchar(255) default thiscardnum;
    DECLARE pgtab varchar(255) default thiscardnum;
    DECLARE path varchar(255);
    -- check if there is a synonym for this table
    SELECT COUNT(*) INTO x FROM
    (SELECT * FROM pgtable_cardnumber_diffs WHERE TABLE_NAME=thiscardnum) AS foo;
    IF x > 0 THEN
       set path = 'Found matches in pgtable_cardnumber';
       -- If there is get the synonym and correct name
       SELECT CardNumber, table_name INTO mycdn, pgtab FROM pgtable_cardnumber_diffs
       WHERE TABLE_NAME=thiscardnum;
       set thiscardnum = mycdn;
    ELSE
       set path = 'No matches found';
    END IF;
	-- Now go ahead and modify all the descriptions for variables in thiscardnum
	INSERT INTO descriptives (NAME, descriptives) 
	SELECT  name, newdescrip FROM variablelabels WHERE CardNumber = thiscardnum
	ON DUPLICATE KEY UPDATE descriptives = VALUES(descriptives);
end $$

delimiter ;
