DELIMITER $$

DROP PROCEDURE IF EXISTS `addCategoryMembers`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `addCategoryMembers`(IN thisCardNumber VARCHAR (255),
												IN thisCode int (10))
    COMMENT 'Add every variable in thisCardNumber to the category thisCode in categorymembers table.'
BEGIN
	-- categorymembers table has fields:
	-- name        varchar(64) 
	-- code        int(10) unsigned
	-- Creator     varchar(20)
	-- CreateDate  datetime
	-- CreatorApp  varchar(20) 
	-- Need to use prepared statement to make execution a two stage process
	SET @st = CONCAT('INSERT INTO categorymembers (name, code, Creator, CreateDate, CreatorApp) ',
		             ' SELECT Name,"',thisCode,'", "PC/IS","',DATE(NOW()),'", "SQL Proc"',
		             ' FROM variablelabels ',
					 ' WHERE CardNumber ="', thisCardNumber,'"');
	PREPARE stmt FROM @st;
	EXECUTE stmt;
END$$
  
DELIMITER ;
  
