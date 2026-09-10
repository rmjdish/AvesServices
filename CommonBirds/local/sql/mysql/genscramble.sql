
DELIMITER $$

DROP PROCEDURE IF EXISTS `gescramble`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `genscramble`(IN username VARCHAR (20))
    COMMENT 'Create a scramble code identified by username using next available sequence Feb 2019'
BEGIN
   DECLARE CUSTOM_EXCEPTION CONDITION FOR SQLSTATE '45000';
   DECLARE nxtseq   VARCHAR(12);
   DECLARE nxttable VARCHAR(12);
   SET nxtseq = nextseqn();
   SET nxttable = seqn2table(nextseqn());
   IF  nxttable = 'XXXXX' THEN
       SIGNAL CUSTOM_EXCEPTION
       SET MESSAGE_TEXT = 'Could not find supplied seqn label';
   END IF;

   INSERT INTO scrambling.users(user, seqtable, colname) VALUES(username,nxttable,nxtseq);
  END$$
  
  DELIMITER ;
  
