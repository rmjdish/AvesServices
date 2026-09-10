DELIMITER $$

DROP PROCEDURE IF EXISTS `addLongitudinalVars`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `addLongitudinalVars`
	(IN thisMeasure VARCHAR (128), thisLongVar VARCHAR (64))
    COMMENT 'Add a variable to a named measure in longitudinalvars table'
BEGIN
INSERT INTO longitudinalvars(measureName, longVar) 
	VALUES (thisMeasure, thisLongVar)
	ON DUPLICATE KEY UPDATE longVar = CONCAT(thisLongVar,DATE(NOW()));
END$$
  
DELIMITER ;
  
