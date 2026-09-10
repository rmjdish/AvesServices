DELIMITER $$

DROP PROCEDURE IF EXISTS `categorySearch`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `categorySearch`(IN seclevel INT(5), IN searchString VARCHAR(255))
    READS SQL DATA
    COMMENT 'Find matches for searchString in category field'
BEGIN
	DECLARE cate INT(11);
	DECLARE theterm VARCHAR(255);
	IF searchString REGEXP '^[+-]*[0-9]+$' THEN
		SET cate = CAST(searchString AS UNSIGNED);
	ELSE
		SET cate = 0;
	END IF;

	SELECT t1.NAME,Label,YEAR,Form,QuestionNumber,CardNumber,Public FROM variablelabels as t1
	INNER JOIN categorymembers as t2 ON (t1.Name = t2.name)
	WHERE (t2.code = cate) AND (seclevel + Public > 1 OR Public = 1)
	ORDER BY PRINTORDER, NAME;

END$$

DELIMITER ;
