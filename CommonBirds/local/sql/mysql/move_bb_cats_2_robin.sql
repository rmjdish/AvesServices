-- Phil Curran November 2023-February 2024
-- MySQL Procedure to populate robin.categorylabels table
DELIMITER $$

USE `robin`$$

DROP PROCEDURE IF EXISTS `move_bb_cats_2_robin`$$

CREATE DEFINER=`root`@`%` PROCEDURE `move_bb_cats_2_robin`()
    COMMENT 'OVERWRITES! category codes,labels,etc. and sets parent id from nshd_showcase'
BEGIN
	-- WARNING: ASSUMES nshd_showcase label_id > 50
	-- remove codes that clash with BB showcase codes
	DELETE FROM robin.categorylabels 
	WHERE CODE IN (SELECT label_id FROM nshd_showcase.label);
	-- insert categories from nshd_showcase if not already in robin
	INSERT INTO robin.categorylabels
	(SELECT label_id, caption, 0, orda, descript FROM nshd_showcase.label);
	-- Now set the parents and printorder of the categories just inserted
	UPDATE robin.categorylabels AS RobCat
	INNER JOIN nshd_showcase.label_tree AS ShwLab
	ON RobCat.code = ShwLab.child_id
	SET RobCat.parent = ShwLab.parent_id,
	    RobCat.printorder = ShwLab.orda;
	END$$

DELIMITER ;