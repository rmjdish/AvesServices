DELIMITER $$

DROP PROCEDURE IF EXISTS `librarySearch`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `librarySearch`(IN seclevel INT(5), IN searchString VARCHAR(255))
    READS SQL DATA
    COMMENT 'Find matches for searchString in library file field'
BEGIN
	DECLARE theterm VARCHAR(255);
	IF STRCMP(searchString,'ALL') = 0 THEN
		SET theterm = '%';
	ELSE
		SET theterm = REPLACE(searchString,'_','\_)';
	END IF;
	SELECT NAME,Label,YEAR,Form,QuestionNumber,CardNumber,Public FROM variablelabels
	WHERE (CardNumber LIKE theterm) AND (seclevel + Public > 1 OR Public = 1)
	ORDER BY NAME;
END$$

DELIMITER ;