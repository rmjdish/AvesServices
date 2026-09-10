-- Phil Curran November 2023
-- SQL Script to update robin/rook databases to be same as rob_lab prototoype
DELIMITER $$
USE robin; $$
-- Missing tables
CREATE TABLE IF NOT EXISTS robin.mapping_table as SELECT * from rob_lab.mapping_table; $$
CREATE TABLE IF NOT EXISTS robin.mapping_valuelabels as SELECT * from rob_lab.mapping_valuelabels; $$
-- Alter Tables
-- Done
/*

ALTER TABLE categorylabels
      ADD COLUMN parent INT(11) ,
      ADD COLUMN printorder INT(11) ,
      MODIFY COLUMN label VARCHAR(100) $$

-- Done      
ALTER TABLE shadowvariables
      ADD COLUMN units VARCHAR(20),
      ADD COLUMN printorder INT(11),
      DROP COLUMN Recommended,
      DROP COLUMN Available,
      DROP COLUMN Uncoded,
      MODIFY COLUMN ReplaceWith VARCHAR(64),
      MODIFY COLUMN Label VARCHAR(512),
      DROP COLUMN Verified $$

ALTER TABLE variablelabels
      ADD COLUMN units VARCHAR(20),
      ADD COLUMN printorder INT(11),
      DROP COLUMN Recommended,
      DROP COLUMN Available,
      DROP COLUMN Uncoded,
      DROP COLUMN Verified,
      MODIFY COLUMN ReplaceWith VARCHAR(64),
      MODIFY COLUMN Label VARCHAR(512) $$
*/
-- Continue From Here
-- Put Audit Triggers In
/*
-- Only in Finch
DROP TRIGGER IF EXISTS `ghost_basketdetails` $$

CREATE TRIGGER `ghost_basketdetails` AFTER INSERT ON `basketdetails`
FOR EACH ROW 
BEGIN
    INSERT INTO ghost_robin.basketdetails 
    VALUES (new.basketID, new.Description, new.username, new.createDate)
    ON DUPLICATE KEY UPDATE basketID = basketID;
END $$

DROP TRIGGER IF EXISTS `ghost_basketdetails_update` $$

CREATE TRIGGER `ghost_basketdetails_update` AFTER UPDATE ON `basketdetails`
FOR EACH ROW 
BEGIN
    UPDATE ghost_robin.basketdetails 
        SET Description = new.Description, 
	    username = new.username, 
	    createDate = new.createDate	
        WHERE basketID = new.basketID;
END $$
*/
-- Only in Kiwi
DROP TRIGGER IF EXISTS `catlab_ins_log` $$

CREATE TRIGGER `catlab_ins_log` AFTER INSERT ON `categorylabels`
FOR EACH ROW
BEGIN
    CALL audit_log('INSERT', 'robin', 'categorylabels',
    	 		     'code', 'NA', CAST(NEW.code AS CHAR));
    CALL audit_log('INSERT', 'robin', 'categorylabels',
    	 		     'label', 'NA', NEW.label);
    CALL audit_log('INSERT', 'robin', 'categorylabels',
    	 		     'parent', 'NA', CAST(NEW.parent AS CHAR));
    CALL audit_log('INSERT', 'robin', 'categorylabels',
    	 		     'printorder', 'NA', CAST(NEW.printorder AS CHAR));
END $$


DROP TRIGGER IF EXISTS `catlab_upd_log` $$

CREATE TRIGGER `catlab_upd_log` AFTER UPDATE ON `categorylabels`
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
END $$


DROP TRIGGER IF EXISTS `catlab_del_log` $$

CREATE TRIGGER `catlab_del_log` BEFORE DELETE ON `categorylabels`
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
END $$

DROP TRIGGER IF EXISTS `catmem_ins_log` $$

CREATE TRIGGER `catmem_ins_log` AFTER INSERT ON `categorymembers`
FOR EACH ROW
BEGIN
    CALL audit_log('INSERT', 'robin', 'categorymembers', 'name', 'NA', NEW.name);
    CALL audit_log('INSERT', 'robin', 'categorymembers', 'code', 'NA', NEW.code);
    CALL audit_log('INSERT', 'robin', 'categorymembers', 'Creator', 'NA', NEW.Creator);
    CALL audit_log('INSERT', 'robin', 'categorymembers', 'CreateDate', 'NA',
	   	CAST(NEW.CreateDate AS CHAR));
    CALL audit_log('INSERT', 'robin', 'categorymembers', 'CreatorApp', 'NA',
	   	NEW.CreatorApp);   	  
END $$


DROP TRIGGER IF EXISTS `catmem_upd_log` $$

CREATE TRIGGER `catmem_upd_log` AFTER UPDATE ON `categorymembers`
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
END $$

DROP TRIGGER IF EXISTS `catmem_del_log` $$

CREATE TRIGGER `catmem_del_log` BEFORE DELETE ON `categorymembers`
FOR EACH ROW
BEGIN
    CALL audit_log('DELETE', 'robin', 'categorymembers',
                   'name', OLD.name, 'NA');
    CALL audit_log('DELETE', 'robin', 'categorymembers', 'code',
                   OLD.code, 'NA');
    CALL audit_log('DELETE', 'robin', 'categorymembers', 'Creator',
                   OLD.Creator, 'NA');
    CALL audit_log('DELETE', 'robin', 'categorymembers', 'CreateDate', 
    	 	   CAST(OLD.CreateDate AS CHAR), 'NA');
    CALL audit_log('DELETE', 'robin', 'categorymembers', 'CreatorApp', 
                   OLD.CreatorApp, 'NA');   	  
END $$

DROP TRIGGER IF EXISTS `ghost_users_insert` $$

CREATE TRIGGER `ghost_users_insert` AFTER INSERT ON `users`
FOR EACH ROW
BEGIN
    INSERT INTO ghost_robin.users (
          username, firstName, lastName, affiliation,
          address1, address2, city, postcode, county,
	  phone, email, password, status, reviewer, CreateDate) 
	VALUES (new.username, new.firstName, new.lastName, new.affiliation,
		new.address1, new.address2, new.city, new.postcode,
		new.county, new.phone, new.email, new.password,
		new.status, new.reviewer, new.CreateDate)
	ON DUPLICATE KEY UPDATE username = username;
END $$

DROP TRIGGER IF EXISTS `ghost_users_update` $$

CREATE TRIGGER `ghost_users_update` AFTER UPDATE ON `users`
FOR EACH ROW
BEGIN
	UPDATE ghost_robin.users 
	SET firstName = new.firstName, 
	    lastName = new.lastName,
	    affiliation = new.affiliation,
	    address1 = new.address1,
	    address2 = new.address2,
	    city = new.city,
	    postcode = new.postcode,
	    county = new.county,
	    phone = new.phone,
	    email = new.email,
	    password = new.password,
	    status = new.status,
	    reviewer = new.reviewer,
	    CreateDate = new.CreateDate 
	WHERE username = new.username;
END $$

DROP TRIGGER IF EXISTS `vallabs_ins_log` $$

CREATE TRIGGER `vallabs_ins_log` AFTER INSERT ON `valuelabels`
FOR EACH ROW
BEGIN
    CALL audit_log('INSERT', 'robin', 'valuelabels', 'Value',
	   	'NA', NEW.Value);
    CALL audit_log('INSERT', 'robin', 'valuelabels', 'Label',
	   	'NA', NEW.Label);
    CALL audit_log('INSERT', 'robin', 'valuelabels', 'MissingValueCode',
	   	'NA', CAST(NEW.MissingValueCode as CHAR));
END $$

DROP TRIGGER IF EXISTS `vallabs_upd_log` $$

CREATE TRIGGER `vallabs_upd_log` AFTER UPDATE ON `valuelabels`
FOR EACH ROW
BEGIN
    CALL audit_log('UPDATE', 'robin', 'valuelabels', 'Value',
	   	OLD.Value, NEW.Value);
    CALL audit_log('UPDATE', 'robin', 'valuelabels', 'Label',
	   	OLD.Label, NEW.Label);
    CALL audit_log('UPDATE', 'robin', 'valuelabels', 'MissingValueCode',
	   	CAST(OLD.MissingValueCode as CHAR), CAST(NEW.MissingValueCode as CHAR));
END $$

DROP TRIGGER IF EXISTS `vallabs_del_log` $$

CREATE TRIGGER `vallabs_del_log` AFTER DELETE ON `valuelabels`
FOR EACH ROW
BEGIN
    CALL audit_log('DELETE', 'robin', 'valuelabels', 'Value',
	   	OLD.Value, 'NA');
    CALL audit_log('DELETE', 'robin', 'valuelabels', 'Label',
	   	OLD.Label, 'NA');
    CALL audit_log('DELETE', 'robin', 'valuelabels', 'MissingValueCode',
	   	CAST(OLD.MissingValueCode as CHAR), 'NA');
END $$

DROP TRIGGER IF EXISTS `varlabs_ins_log` $$

CREATE TRIGGER `varlabs_ins_log` AFTER INSERT ON `variablelabels`
FOR EACH ROW
BEGIN
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
END $$


DROP TRIGGER IF EXISTS `varlabs_upd_log` $$

CREATE TRIGGER `varlabs_upd_log` AFTER UPDATE ON `variablelabels`
FOR EACH ROW
BEGIN
   CALL audit_log('UPDATE', 'robin', 'variablelabels',
                  'CardNumber',OLD.CardNumber, NEW.CardNumber);
   CALL audit_log('UPDATE', 'robin', 'variablelabels',
                  'ColStart',OLD.ColStart, NEW.ColStart);
   CALL audit_log('UPDATE', 'robin', 'variablelabels',
                  'ColEnd',OLD.ColEnd, NEW.ColEnd);
   CALL audit_log('UPDATE', 'robin', 'variablelabels',
                  'Label',OLD.Label, NEW.Label);
   CALL audit_log('UPDATE', 'robin', 'variablelabels',
                  'Form',OLD.Form, NEW.Form);
   CALL audit_log('UPDATE', 'robin', 'variablelabels',
                  'QuestionNumber',OLD.QuestionNumber, NEW.QuestionNumber);
   CALL audit_log('UPDATE', 'robin', 'variablelabels',
                  'YEAR',OLD.YEAR, NEW.YEAR);
   CALL audit_log('UPDATE', 'robin', 'variablelabels',
                   'Derived',CAST(OLD.Derived as CHAR),
		   CAST(NEW.Derived as CHAR));
   CALL audit_log('UPDATE', 'robin', 'variablelabels',
                  'ReplaceWith',OLD.ReplaceWith, NEW.ReplaceWith);
   CALL audit_log('UPDATE', 'robin', 'variablelabels',
                  'Creator',OLD.Creator, NEW.Creator);
   CALL audit_log('UPDATE', 'robin', 'variablelabels',
                  'CreateDate',CAST(OLD.CreateDate AS CHAR),
		  CAST(NEW.CreateDate AS CHAR));
   CALL audit_log('UPDATE', 'robin', 'variablelabels',
                  'CreatorApp',OLD.CreatorApp, NEW.CreatorApp);
   CALL audit_log('UPDATE', 'robin', 'variablelabels',
                  'Public',CAST(OLD.Public AS CHAR),
		  CAST(NEW.Public AS CHAR));
   CALL audit_log('UPDATE', 'robin', 'variablelabels',
                  'units',OLD.units, NEW.units);
   CALL audit_log('UPDATE', 'robin', 'variablelabels',
                  'printorder',CAST(OLD.printorder AS CHAR),
		  CAST(NEW.printorder AS CHAR));
END $$

DROP TRIGGER IF EXISTS `varlabs_del_log` $$

CREATE TRIGGER `varlabs_del_log` AFTER DELETE ON `variablelabels`
FOR EACH ROW
BEGIN
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
END $$
DELIMITER ;


