DELIMITER $$
-- Adds entries in audit.history table for updates to variablelabels fields
    DROP TRIGGER IF EXISTS `robin`.`varlabs_upd_log`$$
    
    CREATE TRIGGER `robin`.`varlabs_upd_log` AFTER UPDATE ON `robin`.`variablelabels`
         FOR EACH ROW
        BEGIN
		   CALL audit_log('UPDATE', 'robin', 'variablelabels', 'CardNumber',OLD.CardNumber, NEW.CardNumber);
		   CALL audit_log('UPDATE', 'robin', 'variablelabels', 'ColStart',OLD.ColStart, NEW.ColStart);
		   CALL audit_log('UPDATE', 'robin', 'variablelabels', 'ColEnd',OLD.ColEnd, NEW.ColEnd);
		   CALL audit_log('UPDATE', 'robin', 'variablelabels', 'Label',OLD.Label, NEW.Label);
		   CALL audit_log('UPDATE', 'robin', 'variablelabels', 'Form',OLD.Form, NEW.Form);
		   CALL audit_log('UPDATE', 'robin', 'variablelabels', 'QuestionNumber',OLD.QuestionNumber, NEW.QuestionNumber);
		   CALL audit_log('UPDATE', 'robin', 'variablelabels', 'YEAR',OLD.YEAR, NEW.YEAR);
		   CALL audit_log('UPDATE', 'robin', 'variablelabels', 'Derived',CAST(OLD.Derived as CHAR), CAST(NEW.Derived as CHAR));
		   CALL audit_log('UPDATE', 'robin', 'variablelabels', 'ReplaceWith',OLD.ReplaceWith, NEW.ReplaceWith);
		   CALL audit_log('UPDATE', 'robin', 'variablelabels', 'Creator',OLD.Creator, NEW.Creator);
		   CALL audit_log('UPDATE', 'robin', 'variablelabels', 'CreateDate',CAST(OLD.CreateDate AS CHAR), CAST(NEW.CreateDate AS CHAR));
		   CALL audit_log('UPDATE', 'robin', 'variablelabels', 'CreatorApp',OLD.CreatorApp, NEW.CreatorApp);
		   CALL audit_log('UPDATE', 'robin', 'variablelabels', 'Public',CAST(OLD.Public AS CHAR), CAST(NEW.Public AS CHAR));
		   CALL audit_log('UPDATE', 'robin', 'variablelabels', 'units',OLD.units, NEW.units);
		   CALL audit_log('UPDATE', 'robin', 'variablelabels', 'printorder',CAST(OLD.printorder AS CHAR), CAST(NEW.printorder AS CHAR));
      END$$
		   
DELIMITER ;
		   
