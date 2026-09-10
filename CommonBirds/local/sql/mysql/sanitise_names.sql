-- Phil Curran November 2023
-- SQL (MySQL) Procedure to get rid of nasty stuff in Names and Value fields
DELIMITER $$

DROP PROCEDURE IF EXISTS sanitise_names$$

CREATE PROCEDURE `sanitise_names`()
    READS SQL DATA
    COMMENT 'Remove non-alphanum chars from name,value fields in varialblelabels and valuelabels'
BEGIN
	-- define the pattern to look for in names
	declare pat VARCHAR(100);
	set pat = "[[:cntrl:]]+|[[:blank:]]+|[\\*']+";
	-- update variablelabels
	update variablelabels
	set name = REGEXP_replace(name, pat, '')
	where NOT NAME RLIKE '^[[:alnum:]]+$';
	-- do the same for valuelabels
	UPDATE valuelabels
	SET NAME = REGEXP_REPLACE(NAME, pat, '')
	WHERE NOT NAME RLIKE '^[[:alnum:]]+$';
	-- and for Value field
	UPDATE valuelabels
	SET Value = REGEXP_REPLACE(Value, pat, '')
	WHERE NOT VALUE RLIKE '^[[:alnum:]]+$';
END $$
DELIMITER ;
