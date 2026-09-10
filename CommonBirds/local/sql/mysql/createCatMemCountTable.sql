-- MySQL routine to create a view of the categorymemberscount table 
-- grouped by code with counts for each
DELIMITER $$

SET GLOBAL event_scheduler = ON$$    
CREATE	DEFINER = CURRENT_USER	
EVENT `robin`.`calc_category_mem_count`
ON SCHEDULE EVERY 1 DAY
COMMENT 'Update the categorymembercount table every day'
DO
BEGIN
  DROP TABLE IF EXISTS robin.categorymembercount;
  CREATE TABLE robin.categorymembercount AS
	SELECT code,label,cat_var_count(code) AS 'items' FROM categorylabels 
	GROUP BY code;
END$$

DELIMITER ;
