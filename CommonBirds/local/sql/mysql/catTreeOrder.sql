-- Phil Curran January 2024 
DELIMITER $$
DROP PROCEDURE IF EXISTS `catTreeOrder` $$
CREATE DEFINER=`root`@`%` PROCEDURE `catTreeOrder`()
READS SQL DATA
COMMENT 'Sort categorylabels into order suitable for tree presentation'
BEGIN
    WITH RECURSIVE decendants AS 
    ( -- base case
        SELECT code, label, parent, printorder
	    FROM categorylabels WHERE parent = 0
	    UNION ALL
	    -- recursive case
	    SELECT child.code, child.label, child.parent, child.printorder
	        FROM  decendants AS parent,  categorylabels AS child where (parent.code = child.parent)
    ) 
    SELECT * FROM decendants;
END $$
DELIMITER ;
