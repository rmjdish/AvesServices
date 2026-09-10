-- This file is a collection of other procedures used to create frequency descriptives and other stuff
-- Phil Curran January 2024
-- It inlcudes: metadata view, getMeta(CardNumber), getLabel(thisvar), getMetaForVar(thisvar),
-- getGroupsForVar(thisvar), getMissingValues(colname), getVarCat(basket), getVLabels(thisvar)
DELIMITER $$
-- Create the metadata view
CREATE OR REPLACE VIEW `metadata` AS (
select
  `t1`.`CardNumber`       AS `Tab`,
  `t1`.`Name`             AS `Name`,
  `t1`.`Label`            AS `VarLabel`,
  `t1`.`Public`           AS `Public`,
  `t2`.`Value`            AS `Value`,
  `t2`.`Label`            AS `ValueLabel`,
  `t2`.`MissingValueCode` AS `Missing`
from (`variablelabels` `t1`
   left join `valuelabels` `t2`
     on (`t1`.`Name` = `t2`.`Name`))
order by `t1`.`CardNumber`) $$

-- This is duplicated at the end of this file with a different name

DROP PROCEDURE IF EXISTS `getMeta` $$
CREATE DEFINER=`root`@`%` PROCEDURE `getMeta`(CardNumber VARCHAR(255))
    READS SQL DATA
    COMMENT 'Returns all variable level metadata for specified CardNumber'
BEGIN
	select * from metadata where Tab = CardNumber;
END $$
-- Just get label for specified variable
DROP PROCEDURE IF EXISTS `getLabel` $$
CREATE DEFINER=`root`@`%` PROCEDURE `getLabel`(thisvar VARCHAR(64))
    COMMENT 'Returns variable label metadata for one variable'
BEGIN
	SELECT Label 
	FROM variablelabels 
	where Name = thisvar;
END $$
-- Complement to getMeta restricted to specified variable
-- Modified to restrict to cell counts > 10 on 11/03/2024
-- This version requires the existence of the frequencies tabe in robin
DROP PROCEDURE IF EXISTS `getMetaForVar` $$
CREATE DEFINER=`root`@`%` PROCEDURE `getMetaForVar`(thisvar VARCHAR(64))
    READS SQL DATA
    COMMENT 'Returns metadata for a specified variable modulo low cell counts'
BEGIN
	DECLARE freqexists INT DEFAULT 0;
	-- Check table exists
	SELECT count(*) INTO freqexists FROM information_schema.TABLES 
	WHERE TABLE_SCHEMA = 'robin' AND TABLE_NAME = 'frequencies';
	IF freqexists = 0 THEN
		-- No frequencies table so just do the basic thing
		SELECT * FROM metadata WHERE Name = thisvar;
	ELSE
		-- Check values against entries in frequencies table
		IF thisvar IN (SELECT distinct fieldname from frequencies) THEN
			SELECT Tab,Name,VarLabel,Public,Value,ValueLabel,Missing
			FROM metadata INNER JOIN frequencies 
			ON (NAME = fieldname AND CONVERT(VALUE, DECIMAL(10,2)) = fieldvalue)
			WHERE Name=thisvar and fieldcount >=10 ;
		ELSE
			-- thisvar is not in frequencies table
			SELECT * FROM metadata WHERE Name = thisvar;
		END IF;
	END IF;
END $$

-- getGroupsForVar returns a list of variables in the same longitudinalvars group
-- if they exist
DROP PROCEDURE IF EXISTS `getGroupsForVar` $$
CREATE DEFINER=`root`@`%` PROCEDURE `getGroupsForVar`(thisvar VARCHAR(64))
    READS SQL DATA
    COMMENT 'Returns list of vars (in search results form) in the same longitudinal vars group, possibly empty'
BEGIN
	DECLARE mygroup VARCHAR(128) DEFAULT NULL;
	-- Check if exists
	SELECT measureName INTO mygroup FROM longitudinalvars 
	WHERE longVar = thisvar;
	IF NOT mygroup IS NULL THEN
		-- thisvar belongs to group mygroup return all except yourself
		SELECT longVar, Label, YEAR, Form, QuestionNumber, CardNumber, Public 
		FROM longitudinalvars LEFT JOIN variablelabels
		ON (longVar = Name)
		WHERE measureName = mygroup and longVar <> thisvar;
	ELSE
		-- thisvar not in a group return empty set
		SELECT Name, Label, YEAR, Form, QuestionNumber, CardNumber, Public 
		FROM variablelabels LIMIT 0;
	END IF;
END $$

-- Return all missing values for a specified variable

DROP PROCEDURE IF EXISTS `getMissingValues` $$
CREATE DEFINER=`root`@`%` PROCEDURE `getMissingValues`(colname VARCHAR(64))
    COMMENT 'Returns all values marked Missing from metadata view'
BEGIN
	select Value from metadata where Name = colname and Missing <> 0;
END $$

-- Get category information on the categories of all variables in a basket

DROP PROCEDURE IF EXISTS `getVarCat` $$
CREATE DEFINER=`root`@`%` PROCEDURE `getVarCat`(IN basket CHARACTER VARYING(20))
    READS SQL DATA
    COMMENT 'Given a basket name, return the list of categories for all variables as a character string'
BEGIN
    DROP TEMPORARY TABLE IF EXISTS bvars;
    CREATE TEMPORARY TABLE bvars
    	   (SELECT name FROM shoppingbaskets WHERE basketID = basket);
    SELECT t1.name,t1.Label,t3.label AS 'Category'
    FROM variablelabels AS t1
    LEFT JOIN categorymembers AS t2 ON t1.name=t2.name
    LEFT JOIN categorylabels AS t3 ON t2.code=t3.code
    WHERE t1.Name IN (select name from bvars) ORDER BY YEAR;
END $$

-- Return valuelabels info for specified variable

DROP PROCEDURE IF EXISTS `getVLabels` $$
CREATE DEFINER=`root`@`%` PROCEDURE `getVLabels`(thisvar VARCHAR(64))
    COMMENT 'Returns all variable labels metadata for one variable'
BEGIN
	SELECT CAST(t2.Value AS SIGNED) as 'Value' ,t2.Label as 'ValueLabel',t2.MissingValueCode
	FROM valuelabels AS t2
	where Name = thisvar;
END $$


DELIMITER ;
