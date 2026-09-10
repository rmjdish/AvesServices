delimiter //
Create or replace procedure audit_log(op VARCHAR(20),
       db VARCHAR(20),
       tab VARCHAR(20),
       fld VARCHAR(20),
       oval VARCHAR(255),
       nval VARCHAR(255))
    COMMENT 'Add audit entry to history table in audit db'
    BEGIN
        INSERT INTO audit.history (tstamp, operation, db, tab, fld, old_value, new_value)
	       VALUES (NOW(), op, db, tab, fld, oval, nval);
    END;//

-- Triggers for categorymembers table
DROP TRIGGER IF EXISTS catmem_ins_log;//

CREATE TRIGGER catmem_ins_log AFTER INSERT ON robin.categorymembers
-- Trigger to audit INSERT operations on categorymembers
       FOR EACH ROW
       BEGIN
           CALL audit_log('INSERT', 'robin', 'categorymembers', 'name', 'NA', NEW.name);
	   CALL audit_log('INSERT', 'robin', 'categorymembers', 'code', 'NA', NEW.code);
	   CALL audit_log('INSERT', 'robin', 'categorymembers', 'Creator', 'NA', NEW.Creator);
	   CALL audit_log('INSERT', 'robin', 'categorymembers', 'CreateDate', 'NA',
	   	CAST(NEW.CreateDate AS CHAR));
	   CALL audit_log('INSERT', 'robin', 'categorymembers', 'CreatorApp', 'NA',
	   	NEW.CreatorApp);   	  
       END;//

DROP TRIGGER IF EXISTS catmem_del_log;//

CREATE TRIGGER catmem_del_log BEFORE DELETE ON robin.categorymembers
-- Trigger to audit DELETE operations on categorymembers
       FOR EACH ROW
       BEGIN
           CALL audit_log('DELETE', 'robin', 'categorymembers', 'name', OLD.name, 'NA');
	   CALL audit_log('DELETE', 'robin', 'categorymembers', 'code', OLD.code, 'NA');
	   CALL audit_log('DELETE', 'robin', 'categorymembers', 'Creator', OLD.Creator, 'NA');
	   CALL audit_log('DELETE', 'robin', 'categorymembers', 'CreateDate', 
	   	CAST(OLD.CreateDate AS CHAR), 'NA');
	   CALL audit_log('DELETE', 'robin', 'categorymembers', 'CreatorApp', 
	   	OLD.CreatorApp, 'NA');   	  
       END;//

DROP TRIGGER IF EXISTS catmem_upd_log;//

CREATE TRIGGER catmem_upd_log AFTER UPDATE ON robin.categorymembers
-- Trigger to audit UPDATE operations on categorymembers
       FOR EACH ROW
       BEGIN
           CALL audit_log('UPDATE', 'robin', 'categorymembers', 'name',
	   	OLD.name, NEW.name);
	   CALL audit_log('UPDATE', 'robin', 'categorymembers', 'code',
	   	OLD.name, NEW.code);
	   CALL audit_log('UPDATE', 'robin', 'categorymembers', 'Creator',
		OLd.Creator,  NEW.Creator);
	   CALL audit_log('UPDATE', 'robin', 'categorymembers', 'CreateDate',
		CAST(OLD.CreateDate AS CHAR), CAST(NEW.CreateDate AS CHAR));
	   CALL audit_log('UPDATE', 'robin', 'categorymembers', 'CreatorApp',
	   	OLD.CreatorApp, NEW.CreatorApp);   	  
       END;//

-- Triggers for categorylabels table

DROP TRIGGER IF EXISTS catlab_ins_log;//

CREATE TRIGGER catlab_ins_log AFTER INSERT ON robin.categorylabels
-- Trigger to audit INSERT operations on categorylabels
       FOR EACH ROW
       BEGIN
           CALL audit_log('INSERT', 'robin', 'categorylabels', 'code', 'NA',
	   	CAST(NEW.code AS CHAR));
	   CALL audit_log('INSERT', 'robin', 'categorylabels', 'label', 'NA', NEW.label);
	   CALL audit_log('INSERT', 'robin', 'categorylabels', 'parent', 'NA',
	   	CAST(NEW.parent AS CHAR));
	   CALL audit_log('INSERT', 'robin', 'categorylabels', 'printorder', 'NA',
	   	CAST(NEW.printorder AS CHAR));
       END;//


DROP TRIGGER IF EXISTS catlab_del_log;//

CREATE TRIGGER catlab_del_log BEFORE DELETE ON robin.categorylabels
-- Trigger to audit DELETE operations on categorylabels
       FOR EACH ROW
       BEGIN
           CALL audit_log('DELETE', 'robin', 'categorylabels', 'code', 
	   	CAST(OLD.code AS CHAR), 'NA');
	   CALL audit_log('DELETE', 'robin', 'categorylabels', 'label',
	   	OLD.label, 'NA');
	   CALL audit_log('DELETE', 'robin', 'categorylabels', 'parent',
	   	CAST(OLD.parent AS CHAR), 'NA');
	   CALL audit_log('DELETE', 'robin', 'categorylabels', 'printorder',
	   	CAST(OLD.printorder AS CHAR), 'NA');
       END;//


DROP TRIGGER IF EXISTS catlab_upd_log;//

CREATE TRIGGER catlab_upd_log AFTER UPDATE ON robin.categorylabels
-- Trigger to audit UPDATE operations on categorylabels
       FOR EACH ROW
       BEGIN
           CALL audit_log('UPDATE', 'robin', 'categorylabels', 'code', 
	   	CAST(OLD.code AS CHAR), CAST(NEW.code AS CHAR));
	   CALL audit_log('UPDATE', 'robin', 'categorylabels', 'label',
	   	OLD.label, NEW.label);
	   CALL audit_log('UPDATE', 'robin', 'categorylabels', 'parent',
	   	CAST(OLD.parent AS CHAR), CAST(NEW.parent AS CHAR));
	   CALL audit_log('UPDATE', 'robin', 'categorylabels', 'printorder',
	   	CAST(OLD.printorder AS CHAR), CAST(NEW.printorder AS CHAR));
       END;//

delimiter ;
