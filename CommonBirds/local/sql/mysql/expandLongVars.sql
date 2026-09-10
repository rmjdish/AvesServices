DELIMITER $$

DROP PROCEDURE IF EXISTS `expandLongVars`$$ 

SET @@GLOBAL.max_sp_recursion_depth = 255$$
SET @@session.max_sp_recursion_depth = 255$$ 

CREATE DEFINER=`root`@`localhost` PROCEDURE `expandLongVars`(IN candVar CHARACTER VARYING(128))
    READS SQL DATA
    COMMENT 'Look up a candidate variable, check for replacement and expand the list to include all longitudinal variables associated with the same measure'
BEGIN
  DECLARE candMeasure VARCHAR (128);
  DECLARE replacement VARCHAR (128) DEFAULT NULL;
  DECLARE done boolean DEFAULT false;
  -- declare NOT FOUND handler
  DECLARE group_name CURSOR FOR SELECT measureName FROM grouplist;
  DECLARE CONTINUE HANDLER
  FOR NOT FOUND SET done = true;
  DROP TEMPORARY TABLE IF EXISTS expandedvars;
  DROP TEMPORARY TABLE IF EXISTS grouplist;
  SELECT ReplaceWith INTO replacement FROM variablelabels WHERE Name = candVar;
  IF replacement IS NOT NULL AND replacement <> '' THEN
    CALL expandLongVars(replacement);
  ELSE
    -- This version returns the varialbe from the last group membership by lex ordering
    CREATE TEMPORARY TABLE IF NOT EXISTS expandedvars AS 
      (SELECT measureName, longVar FROM longitudinalvars ORDER BY measureName DESC);
    CREATE TEMPORARY TABLE IF NOT EXISTS grouplist AS 
      (SELECT measureName FROM longitudinalvars WHERE longVar = candVar);
    -- open the cursor
    OPEN group_name;
    -- Now loop over the grouplist using cursor defined above
    process_groups: LOOP
      FETCH group_name INTO candMeasure;
      -- End of results?
      IF done = true THEN 
	LEAVE process_groups;
      END IF;
      -- do nothing
    END LOOP;
    CLOSE group_name;
    -- The last group found will be in candMeasure
    -- Get on with it
    IF candMeasure is NULL THEN
      select * from variablelabels where Name = candVar;
    ELSE
      -- The next statement will delete everything except current measureName
      DELETE FROM expandedvars WHERE STRCMP(measureName,candMeasure) != 0;
      SELECT * FROM variablelabels INNER JOIN expandedvars ON Name = longVar;
    END IF;
  END IF;
END$$
DELIMITER ;
