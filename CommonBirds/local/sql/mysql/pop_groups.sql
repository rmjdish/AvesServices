DELIMITER $$
-- Phil Curran February 2024
-- SQL Routine to populate longitudinal vars table from mapping_table
USE robin$$

DROP PROCEDURE IF EXISTS pop_groups$$

CREATE PROCEDURE pop_groups()
BEGIN
  DECLARE done tinyint DEFAULT FALSE;
  DECLARE lastM,grpsize,numins,numout INTEGER DEFAULT 0;
  DECLARE ID, field_id, mName INTEGER;
  DECLARE vName VARCHAR(64);
  DECLARE map_cur CURSOR FOR SELECT Name,field_id,measureName
			   FROM robin.mapping_table order by measureName;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

  OPEN map_cur;
  read_loop: LOOP
    FETCH map_cur INTO vName, field_id, mName;
    -- First check if end of loop
    IF done THEN
      LEAVE read_loop;
    END IF;
    -- Check if in same group
    IF lastM <> mName THEN
      -- New entry or begining of loop
      IF grpsize = 1 AND lastM <> 0 THEN
	  	-- Get rid of non group lastM
		DELETE FROM robin.longitudinalvars
		WHERE measureName = lastM;
      END IF;
      -- Move on to start potential new group
      SET grpsize = 1, lastM = mName;
      -- Get rid of any previous groups with this name
      DELETE FROM robin.longitudinalvars
       WHERE measureName = CAST(mName as CHAR(128));
      -- Start new group with this record
      INSERT INTO robin.longitudinalvars (measureName, longVar)
      VALUES (CAST(mName as CHAR(128)), vName);
      SET numins = numins + 1;      
    ELSE
      -- Record is in group because lastM == nName
      SET grpsize = grpsize + 1;
      -- Add this to longitudinal vars
      INSERT INTO robin.longitudinalvars (measureName, longVar)
      VALUES (CAST(mName as CHAR(128)), vName);
      SET numins = numins + 1;
    END IF;
  END LOOP;
  -- Clean up
  CLOSE map_cur;
  SELECT numins as 'Num Records Inserted';
END; $$
DELIMITER ;
