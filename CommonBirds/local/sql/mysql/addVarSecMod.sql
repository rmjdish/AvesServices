DELIMITER $$

DROP PROCEDURE IF EXISTS `addVarSecMod`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `addVarSecMod`
	(IN thisName VARCHAR (64), IN thisSecLevel tinyint (3), IN thisMessageId int(11))
    COMMENT 'Insert an entry into the varsecmod table for a variable.'
BEGIN
INSERT INTO varsecmod(Name, secLevel, messageId) 
	VALUES (thisName, thisSecLevel, thisMessageId)
	ON DUPLICATE KEY UPDATE Name = CONCAT(thisName,DATE(NOW()));
END$$
  
DELIMITER ;
  
