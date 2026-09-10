DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `approveform`(IN newfid INTEGER, IN newpid VARCHAR(16))
    MODIFIES SQL DATA
    COMMENT 'Adams SQL procedure to approve a form - hacked to avoid MySQL Error returns and always return string message'
BEGIN
DECLARE fid_check INT DEFAULT 0;
DECLARE pid_check VARCHAR (16) DEFAULT "NULL";
DECLARE insight_check INT;
DECLARE ucl_check INT DEFAULT 0;
DECLARE already_app INT DEFAULT 0;
DECLARE pemail VARCHAR (256);
DECLARE messageback VARCHAR (256);
DECLARE proceed INT DEFAULT 1;
/*
Check that there is a form with the id newfid in the forms table; recall formid is a primary key
*/      
SELECT formid INTO fid_check FROM jay.forms WHERE formid=newfid;
IF (fid_check = 0) THEN 
	SELECT 'The Form ID you entered is not found in the forms table, please check and try another' INTO messageback;
	SET proceed = 0;
END IF;
/*
Check that there is NOT a project with pid the same as newpid; proceed = 0 => stop this
*/ 
select pid into pid_check from jay.projects where pid=newpid;
IF (pid_check != "NULL") then
	SELECT 'The Project ID you entered already exists, you cannot use this PID' INTO messageback;
	SET proceed = 0;
END IF;
/*
Set a number of flag parameters based on field values in the forms table
*/
SELECT insight46 INTO insight_check FROM jay.forms WHERE formid=newfid;  
SELECT 1 INTO ucl_check FROM jay.forms WHERE formid=newfid AND prinappemail LIKE '%ucl.ac.uk';
SELECT 1 INTO already_app FROM jay.forms WHERE formid=newfid AND prinapp in (select prinapp from forms where formid != newfid);
/*
Give me Go/No-Go
*/
IF (proceed > 0) THEN
   	BEGIN
   	# Need to remember principal applicants email
   	SELECT prinappemail INTO pemail FROM jay.forms WHERE formid=newfid;
	# Put entry into projects table
 	INSERT INTO jay.projects (pid, dapproved, formid ) 
      	       SELECT newpid,DATE(NOW()),formid 
      	       FROM jay.forms WHERE forms.formid = newfid; 
	# put entry into applicants table for applicant and coapplicant
	INSERT INTO jay.applicants (appname, apppos, appinst, appemail, formid)
     	       SELECT left(prinapp,128), left(prinapppos,128), left(prinappinst,128), left(prinappemail,128), formid
	       FROM jay.forms WHERE forms.formid = newfid;
	INSERT INTO jay.applicants (appname, apppos, appinst, appemail, formid)
     	       SELECT left(coapp,128), left(coapppos,128), left(coappinst,128), left(coappemail,128), formid
	       FROM jay.forms WHERE forms.formid = newfid;
	# Is this non-Insight46 and non-UCL; put entry into progress table status = 1
	IF insight_check !=1 and ucl_check !=1 THEN
	   INSERT INTO jay.progress (pid, status, docpath) VALUES (newpid, 1, CONCAT('\\\\ad.ucl.ac.uk\\groupfolders\\MRCLHA_SST_Meetings\\DSForms\\',newpid));
	# Is this non-Insight but is UCL; put entry into progress table status = 5
 	ELSEIF insight_check !=1 and ucl_check =1 THEN
	       INSERT INTO jay.progress (pid, status, docpath) VALUES (newpid, 5, CONCAT('\\\\ad.ucl.ac.uk\\groupfolders\\MRCLHA_SST_Meetings\\DSForms\\',newpid));
	# Is this Insight46 and is UCL; put entry into progress table status = 37
 	ELSEIF insight_check = 1 and ucl_check =1 THEN
	       INSERT INTO jay.progress (pid, status, docpath) VALUES (newpid, 37, CONCAT('\\\\ad.ucl.ac.uk\\groupfolders\\MRCLHA_SST_Meetings\\DSForms\\',newpid));
	# Is this Insight46 but non-UCL; put entry into progress table status = 33
	ELSEIF insight_check = 1 and ucl_check !=1 THEN
	       INSERT INTO jay.progress (pid, status, docpath) VALUES (newpid, 33, CONCAT('\\\\ad.ucl.ac.uk\\groupfolders\\MRCLHA_SST_Meetings\\DSForms\\',newpid));
	# This is Insight46 and is UCL; put entry into progress table status = 37
	ELSE
	       INSERT INTO jay.progress (pid, STATUS, docpath) VALUES (newpid, 37, CONCAT('\\\\ad.ucl.ac.uk\\groupfolders\\MRCLHA_SST_Meetings\\DSForms\\',newpid));
 	END IF ;
 	# if the primary applicant has already submitted a project, they default to 'yes' for 'conf form' and 'data induction' done
 	if already_app =1 then update progress set status=status+24 where pid=newpid; 
 	end if;
 	# if existing form and principal applicant is from UCL
	IF fid_check = newfid and pemail like '%ucl.ac.uk' THEN
	   update progress SET STATUS = STATUS+4 where pid = newpid;
	END IF;
	SELECT 'Approve project successful' INTO messageback;
	END;  
END IF;
SELECT messageback;
END ;;
