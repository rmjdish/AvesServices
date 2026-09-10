DELIMITER $$
DROP TRIGGER IF EXISTS `robin`.`varlabs_ins_log` $$
CREATE TRIGGER `robin`.`varlabs_ins_log` AFTER INSERT
    ON `robin`.`variablelabels`
    FOR EACH ROW BEGIN
       CALL audit_log('INSERT', 'robin', 'variablelabels', 'CardNumber',
	   	'NA', NEW.CardNumber);
       CALL audit_log('INSERT', 'robin', 'variablelabels', 'ColStart',
	   	'NA', NEW.ColStart);
       CALL audit_log('INSERT', 'robin', 'variablelabels', 'ColEnd',
	   	'NA', NEW.ColEnd);
       CALL audit_log('INSERT', 'robin', 'variablelabels', 'Label',
	   	'NA', NEW.Label);
       CALL audit_log('INSERT', 'robin', 'variablelabels', 'Form',
	   	'NA', NEW.Form);
       CALL audit_log('INSERT', 'robin', 'variablelabels', 'QuestionNumber',
	   	'NA', NEW.QuestionNumber);
       CALL audit_log('INSERT', 'robin', 'variablelabels', 'YEAR',
	   	'NA', NEW.YEAR);
       CALL audit_log('INSERT', 'robin', 'variablelabels', 'Derived',
	   	'NA', CAST(NEW.Derived as CHAR));
       CALL audit_log('INSERT', 'robin', 'variablelabels', 'ReplaceWith',
	   	'NA', NEW.ReplaceWith);
       CALL audit_log('INSERT', 'robin', 'variablelabels', 'Creator',
	   	'NA', NEW.Creator);
       CALL audit_log('INSERT', 'robin', 'variablelabels', 'CreateDate',
		'NA', CAST(NEW.CreateDate AS CHAR));
       CALL audit_log('INSERT', 'robin', 'variablelabels', 'CreatorApp',
	   	'NA', NEW.CreatorApp);
       CALL audit_log('INSERT', 'robin', 'variablelabels', 'Public',
	   	'NA', CAST(NEW.Public AS CHAR));
       CALL audit_log('INSERT', 'robin', 'variablelabels', 'units',
	   	'NA', NEW.units);
       CALL audit_log('INSERT', 'robin', 'variablelabels', 'printorder',
	   	'NA', CAST(NEW.printorder AS CHAR));
    END$$

DELIMITER ;
