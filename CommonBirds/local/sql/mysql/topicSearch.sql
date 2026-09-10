DELIMITER $$

DROP PROCEDURE IF EXISTS `topicSearch`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `topicSearch`(IN seclevel INT(5), IN searchString VARCHAR(255))
    READS SQL DATA
    COMMENT 'Find matches for searchString in topic element of keywords table'
BEGIN
	DECLARE theterm VARCHAR(255);
	IF STRCMP(searchString,'*') = 0 THEN
		SET theterm = '%';
	ELSE
		SET theterm = searchString;
	END IF;
	SELECT t1.Name,t1.Label,t1.year,t1.Form,t1.QuestionNumber,t1.CardNumber,t1.Public FROM variablelabels as t1
    JOIN keywords as t2 ON (t1.Name = t2.Name)
	WHERE (getTopicFromKeywords(t2.term) = theterm) AND (seclevel + Public > 1 OR Public = 1)
	ORDER BY NAME;
END$$

DELIMITER ;