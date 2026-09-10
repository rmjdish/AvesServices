-- Phil Curran November 2023 - February 2024
-- SQL Procedure to add category membership for variables in mapping_table to categorymembers table
DELIMITER $$

DROP PROCEDURE IF EXISTS `robin`.`add_bb_catmems_2_robin`$$

CREATE PROCEDURE `robin`.`add_bb_catmems_2_robin`()
COMMENT 'Reads membership info from mapping_table joined with categorylabels and inserts to categorymembers'
BEGIN
  DELETE FROM robin.categorymembers WHERE code > 50; 
  INSERT IGNORE INTO robin.categorymembers
    (SELECT t1.Name, t1.catcode, 'IS/FC', NOW(), 'SQL: add_bb_catmems_2_robin'
     FROM mapping_table t1
     LEFT JOIN categorylabels t2
     ON (t1.catcode = t2.code));
END$$
DELIMITER ;

