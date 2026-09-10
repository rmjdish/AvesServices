-- Adds entry in audit.history for tracked tables/fields
DELIMITER $$
-- Must not be any spaces between last statement and DELIMITER ; command
USE `robin`$$

DROP PROCEDURE IF EXISTS `audit_log`$$

CREATE DEFINER=`root`@`%` PROCEDURE `audit_log`(
   op VARCHAR(20),
   db VARCHAR(20),
   tab VARCHAR(20),
   fld VARCHAR(20),
   oval VARCHAR(512),
   nval VARCHAR(512))
   COMMENT 'Add audit entry to history table in audit db'
   BEGIN
     INSERT INTO audit.history (tstamp, operation, db, tab, fld, old_value, new_value)
     VALUES (NOW(), op, db, tab, fld, oval, nval);
   END$$
DELIMITER ;
    
