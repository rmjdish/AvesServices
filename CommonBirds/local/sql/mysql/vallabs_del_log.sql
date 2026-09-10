DELIMITER $$
DROP TRIGGER IF EXISTS `robin`.`vallabs_del_log` $$
CREATE TRIGGER `robin`.`vallabs_del_log` AFTER DELETE
    ON `robin`.`valuelabels`
    FOR EACH ROW BEGIN
       CALL audit_log('DELETE', 'robin', 'valuelabels', 'Value',
	   	OLD.Value, 'NA');
       CALL audit_log('DELETE', 'robin', 'valuelabels', 'Label',
	   	OLD.Label, 'NA');
       CALL audit_log('DELETE', 'robin', 'valuelabels', 'MissingValueCode',
	   	CAST(OLD.MissingValueCode as CHAR), 'NA');
    END$$

DELIMITER ;
