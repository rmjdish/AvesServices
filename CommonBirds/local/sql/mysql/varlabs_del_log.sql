DELIMITER $$
DROP TRIGGER IF EXISTS `robin`.`varlabs_del_log` $$
  
CREATE TRIGGER `robin`.`varlabs_del_log` AFTER DELETE
    ON `robin`.`variablelabels`
    FOR EACH ROW BEGIN
       CALL audit_log('DELETE', 'robin', 'variablelabels', 'CardNumber',
	   	OLD.CardNumber, 'NA');
       CALL audit_log('DELETE', 'robin', 'variablelabels', 'ColStart',
	   	OLD.ColStart, 'NA');
       CALL audit_log('DELETE', 'robin', 'variablelabels', 'ColEnd',
	   	OLD.ColEnd, 'NA');
       CALL audit_log('DELETE', 'robin', 'variablelabels', 'Label',
	   	OLD.Label, 'NA');
       CALL audit_log('DELETE', 'robin', 'variablelabels', 'Form',
	   	OLD.Form, 'NA');
       CALL audit_log('DELETE', 'robin', 'variablelabels', 'QuestionNumber',
	   	OLD.QuestionNumber, 'NA');
       CALL audit_log('DELETE', 'robin', 'variablelabels', 'YEAR',
	   	OLD.YEAR, 'NA');
       CALL audit_log('DELETE', 'robin', 'variablelabels', 'Derived',
	   	CAST(OLD.Derived as CHAR), 'NA');
       CALL audit_log('DELETE', 'robin', 'variablelabels', 'ReplaceWith',
	   	OLD.ReplaceWith, 'NA');
       CALL audit_log('DELETE', 'robin', 'variablelabels', 'Creator',
	   	OLD.Creator, 'NA');
       CALL audit_log('DELETE', 'robin', 'variablelabels', 'CreateDate',
	       CAST(OLD.CreateDate AS CHAR), 'NA');
       CALL audit_log('DELETE', 'robin', 'variablelabels', 'CreatorApp',
	   	OLD.CreatorApp, 'NA');
       CALL audit_log('DELETE', 'robin', 'variablelabels', 'Public',
	   	CAST(OLD.Public AS CHAR), 'NA');
       CALL audit_log('DELETE', 'robin', 'variablelabels', 'units',
	   	OLD.units, 'NA');
       CALL audit_log('DELETE', 'robin', 'variablelabels', 'printorder',
	   	CAST(OLD.printorder AS CHAR), 'NA');
    END$$

DELIMITER ;
