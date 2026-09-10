DELIMITER $$
DROP TRIGGER IF EXISTS `robin`.`vallabs_ins_log` $$
CREATE TRIGGER `robin`.`vallabs_ins_log` AFTER INSERT
    ON `robin`.`valuelabels`
    FOR EACH ROW BEGIN
       CALL audit_log('INSERT', 'robin', 'valuelabels', 'Value',
	   	'NA', NEW.Value);
       CALL audit_log('INSERT', 'robin', 'valuelabels', 'Label',
	   	'NA', NEW.Label);
       CALL audit_log('INSERT', 'robin', 'valuelabels', 'MissingValueCode',
	   	'NA', CAST(NEW.MissingValueCode as CHAR));
    END$$

DELIMITER ;
