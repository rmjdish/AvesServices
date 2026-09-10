DELIMITER $$

USE `rob_lab`$$

DROP PROCEDURE IF EXISTS `sanitise_names`$$

CREATE DEFINER=`root`@`%` PROCEDURE `sanitise_names`()
    COMMENT 'Remove non-alphanum chars from name field in varialblelabels and valuelabels'
BEGIN
	-- define the pattern to look for in names
	DECLARE pat VARCHAR(100);
	SET pat = "[[:cntrl:]]+|[[:blank:]]+";
	-- update variablelabels
	UPDATE variablelabels
	SET NAME = REGEXP_REPLACE(NAME, pat, '')
	WHERE NOT REGEXP_LIKE(NAME, '[:alnum:]+$');
	-- do the same for valuelabels
	UPDATE valuelabels
	SET NAME = REGEXP_REPLACE(NAME, pat, '')
	WHERE NOT REGEXP_LIKE(NAME, '[:alnum:]+$');
	END$$

DELIMITER ;