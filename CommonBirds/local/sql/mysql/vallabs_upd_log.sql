DELIMITER $$
DROP TRIGGER IF EXISTS `robin`.`vallabs_upd_log`$$
  
CREATE TRIGGER `robin`.`vallabs_upd_log` AFTER UPDATE
    ON `robin`.`valuelabels`
    FOR EACH ROW BEGIN
       CALL audit_log('UPDATE', 'robin', 'valuelabels', 'Value',
	   	OLD.Value, NEW.Value);
       CALL audit_log('UPDATE', 'robin', 'valuelabels', 'Label',
	   	OLD.Label, NEW.Label);
       CALL audit_log('UPDATE', 'robin', 'valuelabels', 'MissingValueCode',
	   	CAST(OLD.MissingValueCode as CHAR), CAST(NEW.MissingValueCode as CHAR));
    END$$

DELIMITER ;
