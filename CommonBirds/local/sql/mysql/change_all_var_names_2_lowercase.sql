-- Phil Curran November 2023
-- Change all variable names to lower case
DELIMITER $$

DROP PROCEDURE IF EXISTS change_all_var_names_2_lowercase$$

CREATE PROCEDURE `change_all_var_names_2_lowercase`()
    MODIFIES SQL DATA
    DETERMINISTIC
    COMMENT 'Changes variable names and CardNumbers to lowercase in tables where they occur'
BEGIN
	-- ***Warning*** only execute this if you know what you're doing!
	UPDATE ignore variablelabels SET NAME = LOWER(NAME),
		CardNumber = LOWER(CardNumber,)
	    ReplaceWith = LOWER(ReplaceWith);
	UPDATE ignore docs SET CardNumber = LOWER(CardNumber);
	UPDATE ignore filepath SET CardNumber = LOWER(CardNumber);
	UPDATE ignore pgtable_cardnumber_diffs SET CardNumber = LOWER(CardNumber);
	UPDATE ignore valuelabels SET NAME = LOWER(NAME);
	UPDATE ignore categorymembers SET NAME = LOWER(NAME), CODE = CODE;
	-- With composite keys must update all elements of key
	UPDATE ignore shoppingbaskets SET username = username,
		basketID = basketID,
		NAME = LOWER(NAME);
	UPDATE ignore varsecmod SET NAME = LOWER(NAME);
	UPDATE ignore shadowvariables SET NAME = LOWER(NAME),
		CardNumber = LOWER(CardNumber);
	UPDATE ignore orphans SET NAME = LOWER(NAME);
	UPDATE ignore longitudinalvars SET longVar = LOWER(longVar);
	UPDATE ignore descriptives SET NAME = LOWER(NAME);
	UPDATE ignore dervars SET NAME = LOWER(NAME);
	-- That's All
END $$
DELIMITER ;
	
