DELIMITER $$

USE `robin`$$

DROP PROCEDURE IF EXISTS `indexSearchUnion`$$

CREATE DEFINER=`root`@`%` PROCEDURE `indexSearchUnion`(IN seclevel INT(5), IN searchString VARCHAR(255))
    READS SQL DATA
    COMMENT 'Find matches for searchString in variable labels'
BEGIN
	DECLARE theterm VARCHAR(255);
	IF STRCMP(searchString,'*') = 0 THEN
		SET theterm = '';
	ELSE
		SET theterm = searchString;
	END IF;
	(SELECT NAME,Label,YEAR,Form,QuestionNumber,CardNumber,Public FROM variablelabels
	WHERE MATCH (Label,NAME,field_id) AGAINST (theterm IN BOOLEAN MODE) AND (seclevel + Public > 1 OR Public = 1))
	UNION
	(SELECT dname AS 'Name', DESCRIPTION AS 'Label', YEAR AS 'Year', 
	NULL AS 'Form', NULL AS 'QuestionNumber', 'File' AS 'CardNumber', 1 AS 'Public' 
	FROM datasets WHERE MATCH (tags) AGAINST (theterm IN BOOLEAN MODE))
	ORDER BY NAME;
END$$

DELIMITER ;
