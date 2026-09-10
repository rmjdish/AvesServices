DELIMITER $$

USE `robin`$$

DROP PROCEDURE IF EXISTS `nameSearch`$$

CREATE DEFINER=`root`@`%` PROCEDURE `nameSearch`(IN seclevel INT(5), IN searchString VARCHAR(255))
    READS SQL DATA
    COMMENT 'Find matches for searchString in variable names'
BEGIN
	DECLARE theterm VARCHAR(255);
	IF STRCMP(searchString,'*') = 0 THEN
		SET theterm = '';
	ELSE
		SET theterm = searchString;
	END IF;
	SELECT NAME,Label,YEAR,Form,QuestionNumber,CardNumber,Public FROM variablelabels
	WHERE MATCH (NAME,field_id) AGAINST (theterm IN BOOLEAN MODE) AND (seclevel + Public > 1 OR Public = 1)
	ORDER BY NAME;
END$$

DELIMITER ;
