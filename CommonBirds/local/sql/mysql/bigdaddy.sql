DELIMITER $$
-- Written by in his very own style by Adam Moore
-- Attempts to understand/reformat/modify by Phil Curran May 2025 
CREATE DEFINER=`root`@`localhost` PROCEDURE `bigdaddy`(IN thispid VARCHAR(256))
    READS SQL DATA
    COMMENT 'Query to unify all the fields that should be returned from the standard Jay report on a single project, \n    The parameter can be a PID, the primary applicant''s full name or a surname! - if applicant / common surname has >1 project, the most recently-approved will be shown'
BEGIN
-- set up the local variables
	DECLARE curstate,binstring VARCHAR (128);
	DECLARE mymax INT;
	
	SET curstate = (
		SELECT `status` 
		FROM progress pp JOIN projects p ON p.pid = pp.pid JOIN forms f ON p.formid=f.formid 
		WHERE pp.pid=thispid OR SUBSTRING_INDEX(f.prinapp,' ',-1) = thispid 
			OR f.prinapp= thispid 
		ORDER BY f.datesub DESC LIMIT 0,1);	
	SELECT MAX(CODE) INTO mymax  FROM statusattributes;
		
    DROP TEMPORARY TABLE IF EXISTS kendo_nagasaki;
    CREATE TEMPORARY TABLE kendo_nagasaki
    	   (Label VARCHAR(127), Information VARCHAR(511));
    	   
	IF curstate >= 0 AND curstate < POW(2,mymax+1)-1 THEN
		BEGIN
		SET binstring = LPAD(BIN(curstate),mymax,"0");
		-- get PID, primary applicant, title
		INSERT INTO kendo_nagasaki(label, information) 
			SELECT 'Project ID', p.pid FROM projects p JOIN forms f 
			ON p.formid=f.formid 
			WHERE p.pid =thispid OR 
			SUBSTRING_INDEX(f.prinapp,' ',-1) = thispid OR 
			f.prinapp= thispid 
			ORDER BY f.datesub DESC 
			LIMIT 0,1;
		INSERT INTO kendo_nagasaki(label, information) 
			SELECT 'Primary applicant', f.prinapp FROM projects p JOIN forms f 
			ON p.formid=f.formid 
			WHERE p.pid =thispid OR 
			SUBSTRING_INDEX(f.prinapp,' ',-1) = thispid OR 
			f.prinapp= thispid 
			ORDER BY f.datesub DESC 
			LIMIT 0,1;
		INSERT INTO kendo_nagasaki(label, information) 
			SELECT 'Project title', SUBSTRING(f.title, 1, 200) FROM projects p JOIN forms f 
			ON p.formid=f.formid 
			WHERE p.pid =thispid OR 
			SUBSTRING_INDEX(f.prinapp,' ',-1) = thispid OR 
			f.prinapp= thispid 
			ORDER BY f.datesub DESC 
			LIMIT 0,1;
		-- get attributes done and not done
		INSERT INTO kendo_nagasaki(label, information) 
			SELECT 'Completed steps',label FROM statusattributes 
			WHERE SUBSTRING(binstring,-CODE,1) = "1"; 
		INSERT INTO kendo_nagasaki(label, information) 
			SELECT 'Uncompleted steps', label  FROM statusattributes 
			WHERE SUBSTRING(binstring,-CODE,1) = "0";	
		-- get most recent activity, how long ago it occurred and how long since the project was approved
		INSERT INTO kendo_nagasaki(label, information) 
			SELECT 'Last activity', label FROM statusattributes 
			WHERE CODE =(SELECT ((LOG(newstatus-oldstatus) + LOG(2))/LOG(2)) AS att 
			FROM statuschanges s JOIN projects p 
			ON p.pid = s.pid JOIN forms f 
			ON p.formid=f.formid 
			WHERE (s.pid=thispid OR SUBSTRING_INDEX(f.prinapp,' ',-1) = thispid OR f.prinapp= thispid) AND
			oldstatus != newstatus 
			ORDER BY statusdate DESC 
			LIMIT 0,1);
		INSERT INTO kendo_nagasaki(label, information) 
			SELECT 'How long since last activity?', 
				CONCAT(DATEDIFF(CURDATE(),
				(SELECT statusdate FROM statuschanges s JOIN projects p 
				ON p.pid = s.pid JOIN forms f 
				ON p.formid=f.formid 
				WHERE s.pid=thispid OR 
				SUBSTRING_INDEX(f.prinapp,' ',-1) = thispid OR 
				f.prinapp= thispid 
				ORDER BY statusdate DESC 
				LIMIT 0,1))," days ago") AS days_ago;
		INSERT INTO kendo_nagasaki(label, information) 
			SELECT 'How long since project approved?', 
			CONCAT(DATEDIFF(CURDATE(),
			(SELECT statusdate FROM statuschanges s JOIN projects p 
			ON p.pid = s.pid JOIN forms f 
			ON p.formid=f.formid 
			WHERE s.pid=thispid OR 
			SUBSTRING_INDEX(f.prinapp,' ',-1) = thispid OR 
			f.prinapp= thispid 
			ORDER BY statusdate ASC 
			LIMIT 0,1)), " days ago") AS toolong;
		-- get document path -- can we make a hotlink in the html?
		INSERT INTO kendo_nagasaki(label, information) 
			SELECT 'Link to project documents', docpath 
			FROM progress pp JOIN projects p 
			ON p.pid = pp.pid JOIN forms f 
			ON p.formid=f.formid 
			WHERE pp.pid=thispid OR 
			SUBSTRING_INDEX(f.prinapp,' ',-1) = thispid OR 
			f.prinapp= thispid 
			ORDER BY f.datesub DESC 
			LIMIT 0,1;
		-- Return what we have
		END
	ELSE
		BEGIN	 
		INSERT INTO kendo_nagasaki(label, information) values ("Uh oh, you made a booboo","Sorry, that's not a Project ID or primary applicant name -- please try again");   
		END;
	END IF;
SELECT * FROM kendo_nagasaki; 
END $$
DELIMITER ;
