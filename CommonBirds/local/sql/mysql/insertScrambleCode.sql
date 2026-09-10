DELIMITER $$

DROP PROCEDURE IF EXISTS `insertScrambleCode`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `insertScrambleCode`(IN pid VARCHAR (20),IN username VARCHAR (20))
    COMMENT 'Insert a mapping between project and a scramble user/code'
BEGIN
INSERT INTO jay.scramblecodes(pid, username) VALUES (pid,username)
	ON DUPLICATE KEY UPDATE username = CONCAT(username,DATE(NOW()));
END$$
  
DELIMITER ;
  
