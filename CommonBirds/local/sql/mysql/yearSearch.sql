DELIMITER $$

DROP PROCEDURE IF EXISTS `yearSearch`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `yearSearch`(IN seclevel INT(5), IN searchString VARCHAR(255))
    READS SQL DATA
    COMMENT 'Find matches for searchString in year field'
BEGIN
	DECLARE theterm VARCHAR(255);
	IF STRCMP(UPPER(searchString),'ALL') = 0 THEN
		SET theterm = '%';
	ELSE
		SET theterm = searchString;
	END IF;
	SELECT NAME,Label,Year,Form,QuestionNumber,CardNumber,Public FROM variablelabels
	WHERE (Year LIKE theterm) AND (seclevel + Public > 1 OR Public = 1)
	ORDER BY NAME;

END$$

DELIMITER ;