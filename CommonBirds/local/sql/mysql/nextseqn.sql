DELIMITER $$

DROP FUNCTION IF EXISTS `nextseqn`$$


CREATE DEFINER=`root`@`localhost` FUNCTION `nextseqn`() RETURNS varchar(12) CHARSET utf8
    READS SQL DATA
    COMMENT 'Returns the next seqn label if available or a random one if they are all used'
BEGIN
    DECLARE navail INT(11) default 1;
    DECLARE top INT(11);
    SELECT MAX(CAST(SUBSTRING(colname,5) AS UNSIGNED))+1 into navail FROM users WHERE colname <>'seqn9999';
    select MAX(upper) into top from catalog;
    if navail < top then
       RETURN CONCAT('seqn',navail);
    else
       RETURN CONCAT('seqn',FLOOR(RAND()*(top-8+1)+8));
    END IF;
END$$

DELIMITER ;

