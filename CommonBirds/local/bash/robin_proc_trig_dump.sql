-- MySQL dump 10.13  Distrib 8.0.40, for Linux (x86_64)
--
-- Host: mrc-kiwi01.ad.ucl.ac.uk    Database: robin
-- ------------------------------------------------------
-- Server version	8.0.31
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `ghost_basketdetails` AFTER INSERT ON `basketdetails` FOR EACH ROW BEGIN
	INSERT INTO ghost_robin.basketdetails VALUES (new.basketID, new.Description, new.username, new.createDate)
	ON DUPLICATE KEY UPDATE basketID = basketID;
    END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `ghost_basketdetails_update` AFTER UPDATE ON `basketdetails` FOR EACH ROW BEGIN
	UPDATE ghost_robin.basketdetails SET Description = new.Description, username = new.username, createDate = new.createDate WHERE basketID = new.basketID;
    END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `catlab_ins_log` AFTER INSERT ON `categorylabels` FOR EACH ROW BEGIN
    CALL audit_log('INSERT', 'robin', 'categorylabels',
    	 		     'code', 'NA', CAST(NEW.code AS CHAR));
    CALL audit_log('INSERT', 'robin', 'categorylabels',
    	 		     'label', 'NA', NEW.label);
    CALL audit_log('INSERT', 'robin', 'categorylabels',
    	 		     'parent', 'NA', CAST(NEW.parent AS CHAR));
    CALL audit_log('INSERT', 'robin', 'categorylabels',
    	 		     'printorder', 'NA', CAST(NEW.printorder AS CHAR));
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `catlab_upd_log` AFTER UPDATE ON `categorylabels` FOR EACH ROW BEGIN
    CALL audit_log('UPDATE', 'robin', 'categorylabels', 'code', 
	   	CAST(OLD.code AS CHAR), CAST(NEW.code AS CHAR));
    CALL audit_log('UPDATE', 'robin', 'categorylabels', 'label',
	   	OLD.label, NEW.label);
    CALL audit_log('UPDATE', 'robin', 'categorylabels', 'parent',
	   	CAST(OLD.parent AS CHAR), CAST(NEW.parent AS CHAR));
    CALL audit_log('UPDATE', 'robin', 'categorylabels', 'printorder',
	   	CAST(OLD.printorder AS CHAR), CAST(NEW.printorder AS CHAR));
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `catlab_del_log` BEFORE DELETE ON `categorylabels` FOR EACH ROW BEGIN
    CALL audit_log('DELETE', 'robin', 'categorylabels', 'code', 
	   	CAST(OLD.code AS CHAR), 'NA');
    CALL audit_log('DELETE', 'robin', 'categorylabels', 'label',
	   	OLD.label, 'NA');
    CALL audit_log('DELETE', 'robin', 'categorylabels', 'parent',
	   	CAST(OLD.parent AS CHAR), 'NA');
    CALL audit_log('DELETE', 'robin', 'categorylabels', 'printorder',
	   	CAST(OLD.printorder AS CHAR), 'NA');
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `catmem_ins_log` AFTER INSERT ON `categorymembers` FOR EACH ROW BEGIN
    CALL audit_log('INSERT', 'robin', 'categorymembers', 'name', 'NA', NEW.name);
    CALL audit_log('INSERT', 'robin', 'categorymembers', 'code', 'NA', NEW.code);
    CALL audit_log('INSERT', 'robin', 'categorymembers', 'Creator', 'NA', NEW.Creator);
    CALL audit_log('INSERT', 'robin', 'categorymembers', 'CreateDate', 'NA',
	   	CAST(NEW.CreateDate AS CHAR));
    CALL audit_log('INSERT', 'robin', 'categorymembers', 'CreatorApp', 'NA',
	   	NEW.CreatorApp);   	  
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `catmem_upd_log` AFTER UPDATE ON `categorymembers` FOR EACH ROW BEGIN
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
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `catmem_del_log` BEFORE DELETE ON `categorymembers` FOR EACH ROW BEGIN
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
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `ghost_shoppingbaskets_insert_trigger` AFTER INSERT ON `shoppingbaskets` FOR EACH ROW BEGIN
	INSERT INTO ghost_robin.shoppingbaskets (username, basketID, NAME) VALUES (new.username, new.basketID, LOWER(new.name))
	ON DUPLICATE KEY UPDATE basketID = basketID;
    END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `ghost_shoppingbaskets_update_trigger` AFTER UPDATE ON `shoppingbaskets` FOR EACH ROW BEGIN
	UPDATE ghost_robin.shoppingbaskets SET username = new.username, NAME = LOWER(new.name)
	WHERE basketID = new.basketID;
    END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `ghost_users_insert` AFTER INSERT ON `users` FOR EACH ROW BEGIN
    INSERT INTO ghost_robin.users (
          username, firstName, lastName, affiliation,
          address1, address2, city, postcode, county,
	  phone, email, password, status, reviewer, CreateDate) 
	VALUES (new.username, new.firstName, new.lastName, new.affiliation,
		new.address1, new.address2, new.city, new.postcode,
		new.county, new.phone, new.email, new.password,
		new.status, new.reviewer, new.CreateDate)
	ON DUPLICATE KEY UPDATE username = username;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `ghost_users_update` AFTER UPDATE ON `users` FOR EACH ROW BEGIN
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
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `vallabs_ins_log` AFTER INSERT ON `valuelabels` FOR EACH ROW BEGIN
       CALL audit_log('INSERT', 'robin', 'valuelabels', 'Value',
	   	'NA', NEW.Value);
       CALL audit_log('INSERT', 'robin', 'valuelabels', 'Label',
	   	'NA', NEW.Label);
       CALL audit_log('INSERT', 'robin', 'valuelabels', 'MissingValueCode',
	   	'NA', CAST(NEW.MissingValueCode as CHAR));
    END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `vallabs_upd_log` AFTER UPDATE ON `valuelabels` FOR EACH ROW BEGIN
       CALL audit_log('UPDATE', 'robin', 'valuelabels', 'Value',
	   	OLD.Value, NEW.Value);
       CALL audit_log('UPDATE', 'robin', 'valuelabels', 'Label',
	   	OLD.Label, NEW.Label);
       CALL audit_log('UPDATE', 'robin', 'valuelabels', 'MissingValueCode',
	   	CAST(OLD.MissingValueCode as CHAR), CAST(NEW.MissingValueCode as CHAR));
    END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `vallabs_del_log` AFTER DELETE ON `valuelabels` FOR EACH ROW BEGIN
       CALL audit_log('DELETE', 'robin', 'valuelabels', 'Value',
	   	OLD.Value, 'NA');
       CALL audit_log('DELETE', 'robin', 'valuelabels', 'Label',
	   	OLD.Label, 'NA');
       CALL audit_log('DELETE', 'robin', 'valuelabels', 'MissingValueCode',
	   	CAST(OLD.MissingValueCode as CHAR), 'NA');
    END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `varlabs_ins_log` AFTER INSERT ON `variablelabels` FOR EACH ROW BEGIN
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
    END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `varlabs_upd_log` AFTER UPDATE ON `variablelabels` FOR EACH ROW BEGIN
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
      END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `varlabs_del_log` AFTER DELETE ON `variablelabels` FOR EACH ROW BEGIN
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
    END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Dumping routines for database 'robin'
--
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `FirstOfThisMonth`() RETURNS date
    DETERMINISTIC
BEGIN
DECLARE thismonth DATE;
SELECT DATE(((PERIOD_ADD
  (EXTRACT(YEAR_MONTH 
     FROM CURDATE()),+0)*100)+1)) INTO thismonth;
RETURN thismonth;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `getTopicFromKeywords`(keyword VARCHAR (255)) RETURNS varchar(255) CHARSET utf8mb3
    READS SQL DATA
    DETERMINISTIC
    COMMENT 'This function takes an entry from the keywords table and returns a topic name or empty string'
BEGIN
	DECLARE topic VARCHAR (255) DEFAULT '';
	DECLARE matstr VARCHAR (20) DEFAULT 'topics:';
	SET topic = SUBSTR(keyword,LOCATE(matstr,keyword)+LENGTH(matstr));
	RETURN topic;
    END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `ntag2serno`(ntag INT(11)) RETURNS int
    READS SQL DATA
    DETERMINISTIC
BEGIN
DECLARE theone INT(11);
SELECT serno INTO theone FROM newnshdid WHERE ntag1 = ntag;
RETURN theone;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `num_cat_items`(acode int(10) unsigned) RETURNS int
    READS SQL DATA
    DETERMINISTIC
begin 
	declare nitems int(10) default 0;
	select items
		into nitems
		from catmemcount 
		where code=acode;
	return nitems;
end ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `userpriv`(cand VARCHAR(20)) RETURNS varchar(20) CHARSET utf8mb3
    READS SQL DATA
    DETERMINISTIC
BEGIN
	DECLARE theone varchar(20);
	select rights into theone from privileges where username = cand;
	return theone;
    END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `addCategoryMembers`(IN thisCardNumber VARCHAR (255),
												IN thisCode int (10))
    COMMENT 'Add every variable in thisCardNumber to the category thisCode in categorymembers table.'
BEGIN
	-- categorymembers table has fields:
	-- name        varchar(64) 
	-- code        int(10) unsigned
	-- Creator     varchar(20)
	-- CreateDate  datetime
	-- CreatorApp  varchar(20) 
	-- Need to use prepared statement to make execution a two stage process
	SET @st = CONCAT('INSERT INTO categorymembers (name, code, Creator, CreateDate, CreatorApp) ',
		             ' SELECT Name,"',thisCode,'", "PC/IS","',DATE(NOW()),'", "SQL Proc"',
		             ' FROM variablelabels ',
					 ' WHERE CardNumber ="', thisCardNumber,'"');
	PREPARE stmt FROM @st;
	EXECUTE stmt;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `add_bb_catmems_2_robin`()
    COMMENT 'Reads membership info from mapping_table joined with categorylabels and inserts to categorymembers'
BEGIN
  DELETE FROM robin.categorymembers WHERE code > 50; 
  INSERT IGNORE INTO robin.categorymembers
    (SELECT t1.Name, t1.catcode, 'IS/FC', NOW(), 'SQL: add_bb_catmems_2_robin'
     FROM mapping_table t1
     LEFT JOIN categorylabels t2
     ON (t1.catcode = t2.code));
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `audit_log`(
   op VARCHAR(20),
   db VARCHAR(20),
   tab VARCHAR(20),
   fld VARCHAR(20),
   oval VARCHAR(512),
   nval VARCHAR(512))
    COMMENT 'Add audit entry to history table in audit db'
BEGIN
     INSERT INTO audit.history (tstamp, operation, db, tab, fld, old_value, new_value)
     VALUES (NOW(), op, db, tab, fld, oval, nval);
   END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `basketsperuser`()
    MODIFIES SQL DATA
    COMMENT 'How many baskets has each user made'
BEGIN

DROP TABLE IF EXISTS basketsperuser;
    CREATE TABLE basketsperuser
    	   (number_of_users int, number_of_baskets int);

INSERT INTO basketsperuser (
 	   
SELECT COUNT(baskets) AS number_of_users, baskets AS number_of_baskets 
FROM (SELECT u.username, COUNT(*) AS baskets FROM users u LEFT JOIN basketdetails b ON u.username = b.username  WHERE b.createdate > '2018-06-01' AND b.createdate < (SELECT firstofthismonth())
AND u.username NOT IN ('adammoore', 'newtest2','phil.test','director')  GROUP BY username
UNION
SELECT u.username,0 FROM users u WHERE u.username IN ('nmpo','i.shah','k.mackinnon','wulan') OR username NOT IN (SELECT b.username FROM basketdetails b WHERE createdate < (SELECT firstofthismonth())) AND u.username NOT REGEXP 'aaa|^ada' 
AND username NOT IN ('phil.test','director') AND STATUS ='APPROVED' AND createdate <  (SELECT firstofthismonth())) AS basbas  GROUP BY baskets ORDER BY number_of_baskets ASC);

select * from basketsperuser;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `categorySearch`(IN seclevel INT(5), IN searchString VARCHAR(255))
    READS SQL DATA
    COMMENT 'Find matches for searchString in category field'
BEGIN
	DECLARE cate INT(11);
	DECLARE theterm VARCHAR(255);
	IF searchString REGEXP '^[+-]*[0-9]+$' THEN
		SET cate = CAST(searchString AS UNSIGNED);
	ELSE
		SET cate = 0;
	END IF;

	SELECT t1.NAME,Label,YEAR,Form,QuestionNumber,CardNumber,Public FROM variablelabels as t1
	INNER JOIN categorymembers as t2 ON (t1.Name = t2.name)
	WHERE (t2.code = cate) AND (seclevel + Public > 1 OR Public = 1)
	ORDER BY PRINTORDER, NAME;

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `catTreeOrder`()
    READS SQL DATA
    COMMENT 'Sort categorylabels into order suitable for tree presentation'
BEGIN
    WITH RECURSIVE decendants AS 
    ( -- base case
        SELECT code, label, parent, printorder
	    FROM categorylabels WHERE parent = 0
	    UNION ALL
	    -- recursive case
	    SELECT child.code, child.label, child.parent, child.printorder
	        FROM  decendants AS parent,  categorylabels AS child where (parent.code = child.parent)
    ) 
    SELECT * FROM decendants;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `change_all_var_names_2_lowercase`()
    MODIFIES SQL DATA
    DETERMINISTIC
    COMMENT 'Changes variable names to lowercase in tables where they occur'
BEGIN
	-- ***Warning*** only execute this if you know what you're doing!
	UPDATE ignore variablelabels SET NAME = LOWER(NAME),
	       	      		     ReplaceWith = LOWER(ReplaceWith);
	UPDATE ignore valuelabels SET NAME = LOWER(NAME);
	UPDATE ignore categorymembers SET NAME = LOWER(NAME), CODE = CODE;
	-- With composite keys must update all elements of key
	UPDATE ignore shoppingbaskets SET username = username,
		basketID = basketID,
		NAME = LOWER(NAME);
	UPDATE ignore varsecmod SET NAME = LOWER(NAME);
	UPDATE ignore shadowvariables SET NAME = LOWER(NAME);
	UPDATE ignore orphans SET NAME = LOWER(NAME);
	UPDATE ignore longitudinalvars SET longVar = LOWER(longVar);
	UPDATE ignore descriptives SET NAME = LOWER(NAME);
	UPDATE ignore dervars SET NAME = LOWER(NAME);
	-- That's All
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `CloserTag`(IN uname CHARACTER VARYING(20))
    MODIFIES SQL DATA
    COMMENT 'Return the CLOSER tag for a SWIFT variable'
BEGIN
select closerlabel from closercv as t1 join categorymembers as t2 on t1.code = t2.code where t2.name = uname; 
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `copybasket`(IN srcbskt CHARACTER VARYING(20), IN trgbskt CHARACTER VARYING (20))
    MODIFIES SQL DATA
    COMMENT 'Copy srcbskt to trgbskt overwriting all vars in trgbskt'
BEGIN
  SET @st1 = CONCAT('CALL deletebasketvars("',trgbskt,'");');
  SET @st2 = CONCAT('CALL copybasketvars("',srcbskt,'","',trgbskt,'");');
  SET @st3 = CONCAT('CALL copybasketdesc("',srcbskt,'","',trgbskt,'");');
  PREPARE stmt1 FROM @st1;  
  PREPARE stmt2 FROM @st2;  
  PREPARE stmt3 FROM @st3;
  EXECUTE stmt1;
  EXECUTE stmt2;
  EXECUTE stmt3;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `copybasketdesc`(IN srcbskt CHARACTER VARYING(20), IN trgbskt CHARACTER VARYING (20))
    MODIFIES SQL DATA
    COMMENT 'Copies basket description from srcbskt to trgbskt in basketdetai'
BEGIN 
    DROP TABLE IF EXISTS temp_basketdetails;
    CREATE TABLE temp_basketdetails SELECT * FROM basketdetails;
    DELETE FROM temp_basketdetails;
    INSERT INTO temp_basketdetails SELECT * FROM basketdetails WHERE basketID = srcbskt;
    SET @st = CONCAT('UPDATE basketdetails ',
    	             'SET Description = (SELECT Description from temp_basketdetails',
		     '                   WHERE basketID ="',srcbskt,'"),',
		     '    createDate =  (SELECT createDate from temp_basketdetails',
		     '                   WHERE basketID ="',srcbskt,'")',
                     ' WHERE basketID ="',trgbskt,'"');
    PREPARE stmt FROM @st;
    EXECUTE stmt;
-- Clean up after yourself
    DROP TABLE temp_basketdetails;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `copybasketvars`(IN srcbskt CHARACTER VARYING(20), IN trgbskt CHARACTER VARYING (20))
    MODIFIES SQL DATA
    COMMENT 'Copies vars from srcbskt to trgbskt in shoppingbaskets table'
BEGIN
    DROP TABLE IF EXISTS temp_baskets;
    CREATE TABLE temp_baskets SELECT * FROM shoppingbaskets;
    SELECT DISTINCT username INTO @myuser FROM basketdetails WHERE basketID = trgbskt;
    SET @st = CONCAT('INSERT INTO shoppingbaskets (username, basketID, name)',
    	             '   SELECT "',@myuser,'", "',trgbskt,'", t2.name',
		     '   FROM temp_baskets AS t2 ',
                     '   WHERE basketID ="', srcbskt,'"');
    PREPARE stmt FROM @st;
    EXECUTE stmt;
-- Clean up after yourself!!
    DROP TABLE temp_baskets;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `deleteAllUserBaskets`(IN uname CHARACTER VARYING(20))
    MODIFIES SQL DATA
    COMMENT 'Delete all baskets belonging to username'
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE curbskt CHARACTER VARYING(20);
    DECLARE mark CURSOR FOR SELECT basketID FROM bsktstodelete;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    DROP TEMPORARY TABLE IF EXISTS bsktstodelete;
    CREATE TEMPORARY TABLE IF NOT EXISTS bsktstodelete AS (SELECT basketID FROM basketdetails WHERE username = uname);
    SELECT COUNT(*), "Baskets to be deleted" FROM bsktstodelete;
    OPEN mark;
    process_loop: LOOP
        FETCH mark INTO curbskt;
        IF done THEN
            LEAVE process_loop;
        END IF;
        CALL deletebasket(curbskt);
    END LOOP;
    CLOSE mark;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `deletebasket`(IN bskt CHARACTER VARYING(20))
    MODIFIES SQL DATA
    COMMENT 'Delete named basket from basketdetails, projectbaskets, sharedbaskets, shoppingbaskets tables'
begin
	call deletebasketvars(bskt);
	set @st1 = concat('delete from basketdetails where basketID = "',bskt,'"');
	set @st2 = concat('delete from projectbaskets where basketID = "',bskt,'"');
	set @st3 = concat('delete from sharedbaskets where basketID = "',bskt,'"');
	prepare stmt1 from @st1;
	prepare stmt2 from @st2;
	prepare stmt3 from @st3;
	execute stmt1;
	execute stmt2;
	execute stmt3;
end ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `deletebasketvars`(IN bskt CHARACTER VARYING(20))
    MODIFIES SQL DATA
    COMMENT 'Delete all variables from a basket in the shoppingbaskets table'
BEGIN
    SET @st = CONCAT('DELETE FROM shoppingbaskets WHERE basketID = "',bskt,'"');
    PREPARE stmt FROM @st;
    EXECUTE stmt; 
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `deleteCard2`(IN cardnum CHARACTER VARYING(20))
    MODIFIES SQL DATA
    COMMENT 'Delete all variables associated with the CardNumber - cardnum'
BEGIN
DELETE FROM v, w, d, c, f USING variablelabels v INNER JOIN valuelabels w ON v.name=w.name INNER JOIN descriptives d ON w.name=d.name 
INNER JOIN categorymembers c ON w.name=c.name JOIN filepath f ON f.cardnumber=v.cardnumber WHERE v.cardnumber = cardnum;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `deleteCardNumber`(IN cardnum CHARACTER VARYING(20))
    MODIFIES SQL DATA
    COMMENT 'Delete all variables associated with the CardNumber - cardnum'
BEGIN
    DROP TEMPORARY TABLE IF EXISTS varstodelete;
    CREATE TEMPORARY TABLE IF NOT EXISTS varstodelete AS (SELECT NAME FROM variablelabels WHERE CardNumber = cardnum);
	SELECT CardNumber,COUNT(*) AS "Similar CardNumbers" FROM variablelabels GROUP BY CardNumber HAVING CardNumber LIKE cardnum;
    SELECT COUNT(*) AS "Variables to be deleted" FROM varstodelete;
    SELECT COUNT(*) AS "Value Labels to be deleted" FROM valuelabels WHERE NAME IN (SELECT * FROM varstodelete);
    SELECT COUNT(*) AS "Variable Categories to be deleted" FROM categorymembers WHERE NAME IN (SELECT * FROM varstodelete);
    SELECT COUNT(*) AS "Variable Descriptives to be deleted" FROM descriptives WHERE NAME IN (SELECT * FROM varstodelete);
    DELETE FROM variablelabels WHERE NAME IN (SELECT * FROM varstodelete);
    DELETE FROM valuelabels WHERE NAME IN (SELECT * FROM varstodelete);
    DELETE FROM categorymembers WHERE NAME IN (SELECT * FROM varstodelete);
    DELETE FROM descriptives WHERE NAME IN (SELECT * FROM varstodelete);
    DELETE FROM filepath WHERE CardNumber = cardnum;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `deleteUser`(IN uname CHARACTER VARYING(20))
    MODIFIES SQL DATA
    COMMENT 'Delete all baskets belonging to username and delete user record itself'
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE curbskt CHARACTER VARYING(20);
    DECLARE mark CURSOR FOR SELECT basketID FROM bsktstodelete;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    DROP TEMPORARY TABLE IF EXISTS bsktstodelete;
    CREATE TEMPORARY TABLE IF NOT EXISTS bsktstodelete AS (SELECT basketID FROM basketdetails WHERE username = uname);
    SELECT COUNT(*), "Baskets to be deleted" FROM bsktstodelete;
    OPEN mark;
    process_loop: LOOP
        FETCH mark INTO curbskt;
        IF done THEN
            LEAVE process_loop;
        END IF;
        CALL deletebasket(curbskt);
    END LOOP;
    CLOSE mark;
    DELETE FROM users WHERE username = uname;
    DELETE FROM privileges WHERE username = uname;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `delete_descriptives_entry`(IN fld varchar(128))
    COMMENT 'Removes a single entry for the field with name fld in descriptives table'
begin
	delete from descriptives where name = fld;
end ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `embargoCardNumber`(IN cardnum CHARACTER VARYING(20))
    MODIFIES SQL DATA
    COMMENT 'Change the security level of all variables associated with the CardNumber - cardnum'
BEGIN
    DROP TEMPORARY TABLE IF EXISTS varstoembargo;
    CREATE TEMPORARY TABLE IF NOT EXISTS varstoembargo AS (SELECT NAME FROM variablelabels WHERE CardNumber = cardnum);
    SELECT CardNumber,COUNT(*) AS "Similar CardNumbers" FROM variablelabels GROUP BY CardNumber HAVING CardNumber LIKE cardnum;
    SELECT COUNT(*) AS "Variables to be embargoed" FROM varstoembargo;
    INSERT INTO varsecmod (SELECT Name, 1, 6 FROM varstoembargo);
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `expandExecute`(IN myquery CHARacter varying(255))
    READS SQL DATA
    COMMENT 'Takes a query string and expands the result to include longitudinal variables if necessary'
BEGIN
	set @st = concat('CREATE TEMPORARY TABLE results1 AS ',myquery);
	prepare stmt1 from @st;
	drop temporary table if exists results1;
	execute stmt1;
	select * from results1;
    END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `expandLongVars`(IN candVar CHARACTER VARYING(128))
    READS SQL DATA
    COMMENT 'Look up a candidate variable, check for replacement and expand the list to include all longitudinal variables associated with the same measure'
BEGIN
    DECLARE candMeasure VARCHAR (128);
    DECLARE replacement VARCHAR (128) DEFAULT NULL;
    DECLARE done boolean DEFAULT false;
    -- declare NOT FOUND handler
    DECLARE group_name CURSOR FOR SELECT measureName FROM grouplist;
	DECLARE CONTINUE HANDLER
        FOR NOT FOUND SET done = true;
    DROP TEMPORARY TABLE IF EXISTS expandedvars;
    DROP TEMPORARY TABLE IF EXISTS grouplist;
    SELECT ReplaceWith INTO replacement FROM variablelabels WHERE Name = candVar;
    IF replacement IS NOT NULL AND replacement <> '' THEN
    	CALL expandLongVars(replacement);
	ELSE
		-- This version returns the varialbe from the last group membership by lex ordering
    	CREATE TEMPORARY TABLE IF NOT EXISTS expandedvars AS 
    	      (SELECT measureName, longVar FROM longitudinalvars ORDER BY measureName DESC);
    	CREATE TEMPORARY TABLE IF NOT EXISTS grouplist AS 
    	      (SELECT measureName FROM longitudinalvars WHERE longVar = candVar);
    	-- open the cursor
    	OPEN group_name;
    	-- Now loop over the grouplist using cursor defined above
    	process_groups: LOOP
    		FETCH group_name INTO candMeasure;
    		-- End of results?
    		IF done = true THEN 
				LEAVE process_groups;
			END IF;
			-- do nothing
			END LOOP;
		CLOSE group_name;
		-- The last group found will be in candMeasure
		-- Get on with it
    	IF candMeasure is NULL THEN
			select * from variablelabels where Name = candVar;
    	ELSE
    		-- The next statement will delete everything except current measureName
			DELETE FROM expandedvars WHERE STRCMP(measureName,candMeasure) != 0;
			SELECT * FROM variablelabels INNER JOIN expandedvars ON Name = longVar;
    		END IF;
    END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `explodeName`(NAME VARCHAR(255))
    READS SQL DATA
    COMMENT 'This procedure takes a string and lists all characers it contains in ASCII format'
BEGIN
	DECLARE theend INT;
	DECLARE i INT;
	SET theend = LENGTH(NAME);
	SET i = 1;
	CREATE TEMPORARY TABLE dropme (idx INT, asciichar INT,achar CHARACTER);
	WHILE (i <= theend)  DO
		INSERT INTO dropme VALUES (i,ASCII(SUBSTRING(NAME,i,1)),SUBSTRING(NAME,i,1));
                SET i=i+1;
        END WHILE;
        SELECT * FROM dropme;
        drop temporary table dropme;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `getGroupsForVar`(thisvar VARCHAR(64))
    READS SQL DATA
    COMMENT 'Returns list of vars (in search results form) in the same longitudinal vars group, possibly empty'
BEGIN
	DECLARE mygroup VARCHAR(128) DEFAULT NULL;
	-- Check if exists
	SELECT measureName INTO mygroup FROM longitudinalvars 
	WHERE longVar = thisvar;
	IF NOT mygroup IS NULL THEN
		-- thisvar belongs to group mygroup return all except yourself
		SELECT longVar, Label, YEAR, Form, QuestionNumber, CardNumber, Public 
		FROM longitudinalvars LEFT JOIN variablelabels
		ON (longVar = Name)
		WHERE measureName = mygroup and longVar <> thisvar;
	ELSE
		-- thisvar not in a group return empty set
		SELECT Name, Label, YEAR, Form, QuestionNumber, CardNumber, Public 
		FROM variablelabels LIMIT 0;
	END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `getLabel`(thisvar VARCHAR(64))
    COMMENT 'Returns variable label metadata for one variable'
BEGIN
	SELECT Label 
	FROM variablelabels 
	where Name = thisvar;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `getMeta`(CardNumber VARCHAR(255))
    READS SQL DATA
    COMMENT 'Returns all variable level metadata for specified CardNumber'
BEGIN
	select * from metadata where Tab = CardNumber;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `getMetaForVar`(thisvar VARCHAR(64))
    READS SQL DATA
    COMMENT 'Returns metadata for a specified variable modulo low cell counts'
BEGIN
	DECLARE freqexists INT DEFAULT 0;
	-- Check table exists
	SELECT count(*) INTO freqexists FROM information_schema.TABLES 
	WHERE TABLE_SCHEMA = 'robin' AND TABLE_NAME = 'frequencies';
	IF freqexists = 0 THEN
		-- No frequencies table so just do the basic thing
		SELECT * FROM metadata WHERE Name = thisvar;
	ELSE
		-- Check values against entries in frequencies table
		IF thisvar IN (SELECT distinct fieldname from frequencies) THEN
			SELECT Tab,Name,VarLabel,Public,Value,ValueLabel,Missing
			FROM metadata INNER JOIN frequencies 
			ON (NAME = fieldname AND CONVERT(VALUE, DECIMAL(10,2)) = fieldvalue)
			WHERE Name=thisvar and fieldcount >=10 ;
		ELSE
			-- thisvar is not in frequencies table
			SELECT * FROM metadata WHERE Name = thisvar;
		END IF;
	END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `getMissingValues`(colname VARCHAR(64))
    COMMENT 'Returns all values marked Missing from metadata view'
BEGIN
	select Value from metadata where Name = colname and Missing <> 0;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `getVarCat`(IN basket CHARACTER VARYING(20))
    READS SQL DATA
    COMMENT 'Given a basket name, return the list of categories for all variables as a character string'
BEGIN
    DROP TEMPORARY TABLE IF EXISTS bvars;
    CREATE TEMPORARY TABLE bvars
    	   (SELECT name FROM shoppingbaskets WHERE basketID = basket);
    SELECT t1.name,t1.Label,t3.label AS 'Category'
    FROM variablelabels AS t1
    LEFT JOIN categorymembers AS t2 ON t1.name=t2.name
    LEFT JOIN categorylabels AS t3 ON t2.code=t3.code
    WHERE t1.Name IN (select name from bvars) ORDER BY YEAR;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `getVLabels`(thisvar VARCHAR(64))
    COMMENT 'Returns all variable labels metadata for one variable'
BEGIN
	SELECT CAST(t2.Value AS SIGNED) as 'Value' ,t2.Label as 'ValueLabel',t2.MissingValueCode
	FROM valuelabels AS t2
	where Name = thisvar;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `get_metadata_4_table`(pgtable VARCHAR(128))
    READS SQL DATA
    COMMENT 'To find corresponding metadata (in view) for a Postgres table and return it'
BEGIN
	DECLARE x integer;
	DECLARE mycdn varchar(255) default 'MyNone';
	DECLARE pgtab varchar(255) default 'PgNone';
	DECLARE path varchar(255);
	-- check if there is a synonym for this table in the postgres_table field of the filepath table
	SELECT COUNT(*) INTO x FROM
	(SELECT * FROM filepath WHERE postgres_table=pgtable and lower(CardNumber) <> postgres_table) AS foo;
	IF x > 0 THEN
	   set path = 'Postgres table name different from CardNumber in filepath';
	   -- If there is get the synonym and use it
	   SELECT CardNumber, postgres_table INTO mycdn, pgtab FROM filepath
	   WHERE postgres_table=pgtable;
	   select * from metadata where Tab = mycdn;
	ELSE
	   set path = 'Postgres and MySQL names match';
	   select * from metadata where Tab = pgtable;
	END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `indexSearch`(IN seclevel INT(5), IN searchString VARCHAR(255))
    READS SQL DATA
    COMMENT 'Find matches for searchString in variable labels'
BEGIN
	DECLARE theterm VARCHAR(255);
	IF STRCMP(searchString,'*') = 0 THEN
		SET theterm = '';
	ELSE
		SET theterm = searchString;
	END IF;
	SELECT NAME,Label,YEAR,Form,QuestionNumber,CardNumber,Public FROM variablelabels
	WHERE MATCH (Label,NAME) AGAINST (theterm IN BOOLEAN MODE) AND (seclevel + Public > 1 OR Public = 1)
	ORDER BY NAME;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `indexSearchUnion`(IN seclevel INT(5), IN searchString VARCHAR(255))
    READS SQL DATA
    COMMENT 'Find matches for searchString in variable labels'
BEGIN
	DECLARE theterm VARCHAR(255);
	IF STRCMP(searchString,'*') = 0 THEN
		SET theterm = '';
	ELSE
		SET theterm = searchString;
	END IF;
	(SELECT NAME,Label,YEAR,Form,QuestionNumber,CardNumber,Public FROM variablelabels
	WHERE MATCH (Label,NAME,field_id) AGAINST (theterm IN BOOLEAN MODE) AND (seclevel + Public > 1 OR Public = 1))
	UNION
	(SELECT dname AS 'Name', DESCRIPTION AS 'Label', YEAR AS 'Year', 
	NULL AS 'Form', NULL AS 'QuestionNumber', 'File' AS 'CardNumber', 1 AS 'Public' 
	FROM datasets WHERE MATCH (tags) AGAINST (theterm IN BOOLEAN MODE))
	ORDER BY NAME;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `librarySearch`(IN seclevel INT(5), IN searchString VARCHAR(255))
    READS SQL DATA
    COMMENT 'Find matches for searchString in library file field'
BEGIN

	DECLARE theterm VARCHAR(255);
	IF STRCMP(searchString,'ALL') = 0 THEN
		SET theterm = '%';
	ELSE
		SET theterm = searchString;
	END IF;
	SELECT NAME,Label,YEAR,Form,QuestionNumber,CardNumber,Public FROM variablelabels
	WHERE (CardNumber LIKE theterm) AND (seclevel + Public > 1 OR Public = 1)
	ORDER BY NAME;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `list_bad_names`()
    COMMENT 'Find bad name field values in valuelabels and variablelabels'
BEGIN
	DECLARE pat VARCHAR(100);
	-- pattern of one or more non-alphanumeric and underscore
	SET pat = "[[:cntrl:]]+|[[:blank:]]+|[\\*]+";
	-- look in union of valuelables + variablelabels
	select name, 'Valulabels' as 'Table', Label from valuelabels 
	where regexp_like(name, pat)
	union
	select Name, 'Variablelabels' as 'Table', Label from variablelabels
	where regexp_like(name, pat);
	END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `modify_descriptives_by_cardnumber`(IN thiscardnum varchar(128), IN newdescrip mediumtext)
    COMMENT 'Procedure to modify every entry for variables having CardNumber thiscardnum in descriptives table'
begin
    DECLARE x integer;
    DECLARE mycdn varchar(255) default thiscardnum;
    DECLARE pgtab varchar(255) default thiscardnum;
    DECLARE path varchar(255);
    -- check if there is a synonym for this table
    SELECT COUNT(*) INTO x FROM
    (SELECT * FROM pgtable_cardnumber_diffs WHERE TABLE_NAME=thiscardnum) AS foo;
    IF x > 0 THEN
       set path = 'Found matches in pgtable_cardnumber';
       -- If there is get the synonym and correct name
       SELECT CardNumber, table_name INTO mycdn, pgtab FROM pgtable_cardnumber_diffs
       WHERE TABLE_NAME=thiscardnum;
       set thiscardnum = mycdn;
    ELSE
       set path = 'No matches found';
    END IF;
    -- Now go ahead and modify all the descriptions for variables in thiscardnum
    INSERT INTO descriptives (NAME, descriptives) 
    SELECT  name, newdescrip FROM variablelabels WHERE CardNumber = thiscardnum
    ON DUPLICATE KEY UPDATE descriptives = VALUES(descriptives);
    SELECT COUNT(*) AS 'NumMod' FROM descriptives WHERE descriptives = newdescrip;
end ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `monitorbasketsize`()
    COMMENT 'Calculates 95th percentile of basket size frequency'
BEGIN
SET SESSION group_concat_max_len = 1000000;
SELECT  SUBSTRING_INDEX(
            SUBSTRING_INDEX(
                GROUP_CONCAT(                 -- 1) make a sorted list of values
                    f.NumVars
                    ORDER BY f.NumVars
                    SEPARATOR ','
                )
            ,   ','                           -- 2) cut at the comma
            ,   0.95 * COUNT(*) + 1         --    at the position beyond the 95% portion
            )
        ,   ','                               -- 3) cut at the comma
        ,   -1                                --    right after the desired list entry
        )                 AS `95th Percentile`
FROM    basketsize AS f;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `monthlystats`()
    MODIFIES SQL DATA
    COMMENT 'Monthly stats update procedures - Total number of users and baskets on Skylark, how many users have N baskets, wiki usage grouped by website section'
BEGIN
CALL usersandbaskets;
CALL basketsperuser;
CALL monthlywiki;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `monthlywiki`()
    MODIFIES SQL DATA
    COMMENT 'Wiki usage stats broken down by category, also updates the overall wiki views table'
BEGIN
drop temporary table if exists temp3;
create temporary table temp3 as select page from dokustats.stats_access where dt < (SELECT LAST_DAY(CURDATE() - INTERVAL 1 MONTH)) and ip NOT LIKE '128.40.118%' ;
# make the temp table - this is for the workings, the final 'display' will be another table...

ALTER TABLE temp3 ADD COLUMN page2 VARCHAR(255);
# add our 'polished' page column

UPDATE temp3 SET page2 = CONCAT(page,':') WHERE page NOT LIKE '%:%';
# add an : at the end where there isn't one 

DELETE FROM temp3 WHERE page2 NOT IN('home:','introduction:');
DELETE FROM temp3 WHERE page REGEXP 'waitfor|^afue|adam|playground';
# remove the hacking pageviews and a couple of test ones

# a couple of corrections needed, pages in the 'wrong' namespace
update temp3 set page2 ='mrepo' where page2 ='introduction:';
UPDATE temp3 SET page2 ='topics' WHERE page ='mrepo:topics';
UPDATE temp3 SET page2 ='home:' WHERE page like '%wiki%';

update temp3 set page2 = LEFT(page,LOCATE(':',page) - 1) where page2 is null;
# main flesh out of page2

update temp3 set page2 = 'topics' where page like '%topics%';
# move the topics pages back into their namespace

#now just need to change the values of 'page2' to look better on a graph

UPDATE temp3 SET page2 = REPLACE(page2, 'home:', 'homepage');
UPDATE temp3 SET page2 = REPLACE(page2, 'mrepo', 'metadata repository');
UPDATE temp3 SET page2 = REPLACE(page2, 'nshd', 'data sharing procedures');
UPDATE temp3 SET page2 = REPLACE(page2, 'topics', 'metadata topics');
# UPDATE temp3 SET page2 = REPLACE(page2, 'home:', 'homepage')


# take the grouped data and put into the display table for graphical export
drop table if exists wikicategoryviews;
create table wikicategoryviews as SELECT 
IFNULL(page2, 'Total') AS Category, 
COUNT(*) AS `Number of views` FROM temp3 GROUP BY page2 WITH ROLLUP;

select * from wikicategoryviews;
#NICE!


## Part 2 -- updating the overall wiki views and monthly totals table
insert into wikipageviews (date, views, `running total`) select (SELECT LAST_DAY(CURDATE() - INTERVAL 1 MONTH)), (select sum((select `number of views` from wikicategoryviews where category='Total') - 
(select `running total` from wikipageviews where date =(SELECT LAST_DAY(CURDATE() - INTERVAL 2 MONTH))))),
(SELECT `number of views` FROM wikicategoryviews WHERE category='Total');
# SWEET! new row is the date at the end of last month, the diffeence between the new running total and last month's total, and the new overall total
select * from wikipageviews;



END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `monthlywiki2`()
    MODIFIES SQL DATA
    COMMENT 'Wiki usage stats, broken down by namespace'
BEGIN
# 3) Wiki usage - views 
SELECT * FROM (SELECT * FROM (SELECT page example_page, COALESCE(cast(LEFT(page,LOCATE(':',page) - 1) as char(20)),'Total') AS namespace, COUNT(*) AS number_of_views FROM dokustats.stats_access 
WHERE ip NOT LIKE '128.40.118%' AND dt < (SELECT firstofthismonth()) GROUP BY namespace WITH ROLLUP) AS total ORDER BY number_of_views ASC) AS interim WHERE number_of_views > 1 ; 
     
     ## excludes LHA IPs (mine varies, was 128.40.118.75, now ...82), my VPN ISD is via 10.36.120.170 
     
### also excludes all pages with only one visit, mostly the 'hacking' attempts
## 'example_page' field is just for reference, might need slight adjust for final figures (e.g. wiki:home page)
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `movecard2light`(IN thiscard VARCHAR(255))
    MODIFIES SQL DATA
    COMMENT 'Finds every variable with CardNumber in shadowvariables and executes movevar2light on them'
BEGIN
	DECLARE done INT DEFAULT FALSE;
	DECLARE thisvar VARCHAR(20);
	DECLARE mov_window CURSOR FOR SELECT NAME FROM shadowvariables WHERE CardNumber = thiscard;
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
	OPEN mov_window;
	
read_loop: LOOP
	FETCH mov_window INTO thisvar;
	IF done THEN
		LEAVE read_loop;
	END IF;
	CALL movevar2light(thisvar);
	END LOOP;
	
	CLOSE mov_window;	
    END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `movecard2shadow`(IN thiscard VARCHAR(255))
    MODIFIES SQL DATA
    COMMENT 'Finds every variable with CardNumber in variablelabels and executes movevar2shadow on them'
BEGIN
	DECLARE done INT DEFAULT FALSE;
	DECLARE thisvar VARCHAR(20);
	DECLARE mov_window CURSOR FOR SELECT NAME FROM variablelabels WHERE CardNumber = thiscard;
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
	OPEN mov_window;
	
read_loop: LOOP
	FETCH mov_window INTO thisvar;
	IF done THEN
		LEAVE read_loop;
	END IF;
	CALL movevar2shadow(thisvar);
	END LOOP;
	
	CLOSE mov_window;	
    END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `movevar2light`(IN thisvar CHARACTER VARYING (20))
    MODIFIES SQL DATA
    COMMENT 'Finds variable in shadowvariables, inserts it into variablelabels and deletes from shadowvariables'
BEGIN
	DECLARE nvar INT(10);
	SELECT COUNT(*) INTO nvar FROM shadowvariables WHERE NAME = thisvar;
	IF nvar > 0 THEN
		/* Found the variable so move it to variablelabels */
		INSERT INTO variablelabels SELECT * FROM shadowvariables WHERE NAME = thisvar;
		DELETE FROM shadowvariables WHERE NAME = thisvar;
	END IF;
    END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `movevar2shadow`(IN thisvar CHARACTER VARYING (20))
    MODIFIES SQL DATA
    COMMENT 'Finds variable in variablelabels, inserts it in shadowvariables and deletes from variable labels'
BEGIN
	DECLARE nvar INT(10);
	SELECT COUNT(*) INTO nvar FROM variablelabels WHERE NAME = thisvar;
	IF nvar > 0 THEN
		/* Found the variable so move it to shadowvariables */
		INSERT INTO shadowvariables SELECT * FROM variablelabels WHERE NAME = thisvar;
		DELETE FROM variablelabels WHERE NAME = thisvar;
	END IF;
    END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `move_bb_cats_2_robin`()
    COMMENT 'OVERWRITES! category codes,labels,etc. and sets parent id from nshd_showcase'
BEGIN
	-- WARNING: ASSUMES nshd_showcase label_id > 50
	-- remove codes that clash with BB showcase codes
	DELETE FROM robin.categorylabels 
	WHERE CODE IN (SELECT label_id FROM nshd_showcase.label);
	-- insert categories from nshd_showcase if not already in robin
	INSERT INTO robin.categorylabels
	(SELECT label_id, caption, 0, orda, descript FROM nshd_showcase.label);
	-- Now set the parents and printorder of the categories just inserted
	UPDATE robin.categorylabels AS RobCat
	INNER JOIN nshd_showcase.label_tree AS ShwLab
	ON RobCat.code = ShwLab.child_id
	SET RobCat.parent = ShwLab.parent_id,
	    RobCat.printorder = ShwLab.orda;
	END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `names2lowercase`()
    COMMENT 'This procedure changes ALL variable names to lower case in variablelabes/valuelabes/shoppingbaskets'
BEGIN
	-- WARNING This procedure updates variable names across the robin database
	UPDATE variablelabels 
		SET NAME = LOWER(NAME);
	UPDATE shadowvariables
		SET NAME = LOWER(NAME);
	UPDATE valuelabels
		SET NAME = LOWER(NAME);
	UPDATE shoppingbaskets
		SET NAME = LOWER(NAME);
	UPDATE keywords
		SET NAME = LOWER(Name);

	END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `nameSearch`(IN seclevel INT(5), IN searchString VARCHAR(255))
    READS SQL DATA
    COMMENT 'Find matches for searchString in variable names'
BEGIN
	DECLARE theterm VARCHAR(255);
	IF STRCMP(searchString,'*') = 0 THEN
		SET theterm = '';
	ELSE
		SET theterm = searchString;
	END IF;
	SELECT NAME,Label,YEAR,Form,QuestionNumber,CardNumber,Public FROM variablelabels
	WHERE MATCH (NAME,field_id) AGAINST (theterm IN BOOLEAN MODE) AND (seclevel + Public > 1 OR Public = 1)
	ORDER BY NAME;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `pop_groups`()
BEGIN
  DECLARE done tinyint DEFAULT FALSE;
  DECLARE lastM,grpsize,numins,numout INTEGER DEFAULT 0;
  DECLARE ID, field_id, mName INTEGER;
  DECLARE vName VARCHAR(64);
  DECLARE map_cur CURSOR FOR SELECT Name,field_id,measureName
			   FROM robin.mapping_table order by measureName;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

  OPEN map_cur;
  read_loop: LOOP
    FETCH map_cur INTO vName, field_id, mName;
    
    IF done THEN
      LEAVE read_loop;
    END IF;
    
    IF lastM <> mName THEN
      
      IF grpsize = 1 AND lastM <> 0 THEN
	  	
		DELETE FROM robin.longitudinalvars
		WHERE measureName = lastM;
      END IF;
      
      SET grpsize = 1, lastM = mName;
      
      DELETE FROM robin.longitudinalvars
       WHERE measureName = CAST(mName as CHAR(128));
      
      INSERT INTO robin.longitudinalvars (measureName, longVar)
      VALUES (CAST(mName as CHAR(128)), vName);
      SET numins = numins + 1;      
    ELSE
      
      SET grpsize = grpsize + 1;
      
      INSERT INTO robin.longitudinalvars (measureName, longVar)
      VALUES (CAST(mName as CHAR(128)), vName);
      SET numins = numins + 1;
    END IF;
  END LOOP;
  
  CLOSE map_cur;
  SELECT numins as 'Num Records Inserted';
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `printBasket`(IN bskt CHARACTER VARYING(20))
    READS SQL DATA
    COMMENT 'Print all the variable elements for a specific basket'
BEGIN
        
    SELECT basketID,Year,CardNumber,t2.name,Label,Form,QuestionNumber FROM users AS t1
        JOIN shoppingbaskets AS t2 ON t1.username=t2.username
        JOIN variablelabels AS t3 ON t2.name=t3.Name
        WHERE basketID = bskt ORDER BY Year;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `printCBasket`(IN bskt CHARACTER VARYING(20))
    READS SQL DATA
    COMMENT 'Print required fields for CLOSER data delivery text files for a specific basket'
BEGIN
    DROP TEMPORARY TABLE IF EXISTS tempbasket;
    /* Current order of fields is
        CardNumber, basketID, Basket Label, Variable Name,
        Variable Label, Column Start, Column End, File Path */
    CREATE TEMPORARY TABLE IF NOT EXISTS tempbasket AS
        (SELECT NAME
            FROM basketdetails AS A JOIN shoppingbaskets AS B ON A.basketID = B.basketID 
            WHERE A.basketID = bskt);
    
    SELECT t1.name AS 'Variable Name',Label,YEAR,Form,QuestionNumber,nshdlabel AS 'NSHD Category',topicID AS 'CLOSER Topic Code' FROM tempbasket AS t1
        JOIN variablelabels AS t3 ON t1.name=t3.Name
        JOIN categorymembers AS t4 ON t3.Name = t4.Name
        LEFT JOIN closercv AS t5 ON t4.code = t5.code;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `printcbasket_dsitinct`(IN bskt CHARACTER VARYING(20))
    READS SQL DATA
    COMMENT 'Print required fields for CLOSER data delivery text files for a specific basket'
BEGIN
    DROP TEMPORARY TABLE IF EXISTS tempbasket;
    /* Current order of fields is
        CardNumber, basketID, Basket Label, Variable Name,
        Variable Label, Column Start, Column End, File Path */
    CREATE TEMPORARY TABLE IF NOT EXISTS tempbasket AS
        (SELECT distinct NAME
            FROM basketdetails AS A JOIN shoppingbaskets AS B ON A.basketID = B.basketID 
            WHERE A.basketID = bskt);
    
    SELECT t1.name AS 'Variable Name',Label,YEAR,Form,QuestionNumber,nshdlabel AS 'NSHD Category',topicID AS 'CLOSER Topic Code' FROM tempbasket AS t1
        JOIN variablelabels AS t3 ON t1.name=t3.Name
        JOIN categorymembers AS t4 ON t3.Name = t4.Name
        LEFT JOIN closercv AS t5 ON t4.code = t5.code WHERE questionnumber > 99;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `sanitise_names`()
    COMMENT 'Remove non-alphanum chars from name field in varialblelabels and valuelabels'
BEGIN
	-- define the pattern to look for in names
	declare pat VARCHAR(100);
	set pat = "[[:cntrl:]]+|[[:blank:]]+|[\\*]+";
	-- update variablelabels
	update variablelabels
	set name = REGEXP_replace(name, pat, '')
	where NOT REGEXP_LIKE(NAME, '[:alnum:]+$');
	-- do the same for valuelabels
	UPDATE valuelabels
	SET NAME = REGEXP_REPLACE(NAME, pat, '')
	WHERE NOT REGEXP_LIKE(NAME, '[:alnum:]+$');
	END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `simSwiftKeySearch`(IN keyword CHARACTER VARYING(128))
    READS SQL DATA
    COMMENT 'Simulate SWIFT Java searches'
BEGIN
    SET @selpart1 = 'select SQL_CACHE name, NULL as Term,NULL as OTerm, label, year, form, questionnumber, cardnumber from variablelabels where ';
    SET @selpart2 = CONCAT('(label like ''%',keyword,'%'' or name like ''%',keyword,'%'') union ');
    SET @selpart3 = 'select t1.name, t1.term, t2.name, t2.label, t2.year, t2.form, t2.questionnumber, t2.cardnumber ';
    SET @selpart4 = 'from keywords as t1 inner join variablelabels as t2 where ';
    SET @selpart5 = CONCAT('(t1.term like \'%',keyword,'%\' and t1.name=t2.name)');
    SET @selpart6 = 'group by t1.name order by cardnumber desc, year, form, name';
    set @myquery  = CONCAT(@selpart1,@selpart2,@selpart3,@selpart4,@selpart5,@selpart6);
    SET @st = CONCAT('CREATE TEMPORARY TABLE results1 AS ',@myquery);
    PREPARE stmt1 FROM @st;
    DROP TEMPORARY TABLE IF EXISTS results1;
    EXECUTE stmt1; # create the table from the query
    SELECT * FROM results1;
    END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `Spit`(IN sterm VARCHAR(255), IN skind VARCHAR(2))
    COMMENT 'Executes a select for all variables matching a keyword term'
BEGIN
	DECLARE theterm VARCHAR(255);
	DECLARE thecmd VARCHAR(2);
	SELECT LOWER(TRIM(sterm)) INTO theterm;
	SELECT LOWER(TRIM(skind)) INTO thecmd;
	IF thecmd = 'k' THEN
		SELECT t1.Name,t1.Label,t2.secLevel,t1.Year,t1.Form,t1.QuestionNumber,t1.CardNumber 
		FROM robin.variablelabels AS t1 LEFT JOIN robin.varsecmod AS t2 USING (`Name`)
		WHERE t1.Public = 1 AND (LOWER(trim(t1.Label)) LIKE CONCAT('%',theterm,'%') OR LOWER(trim(t1.Name)) LIKE CONCAT('%',theterm,'%'))
		UNION
		SELECT t3.`Name`,t3.Label,t4.secLevel,t3.Year,t3.Form,t3.QuestionNumber,t3.Cardnumber 
		FROM robin.variablelabels AS t3 LEFT JOIN robin.varsecmod AS t4 USING (`Name`) 
		INNER JOIN robin.keywords AS t5 USING (`Name`)
		WHERE t3.Public = 1 AND (STRCMP(LOWER(t5.term),theterm) = 0) 
		GROUP BY NAME
		ORDER BY CardNumber, YEAR, Form, CAST(QuestionNumber AS UNSIGNED), NAME;
	ELSE
		SELECT t1.Name,t1.Label,t2.secLevel,t1.Year,t1.Form,t1.QuestionNumber,t1.Cardnumber 
		FROM robin.variablelabels AS t1 LEFT JOIN robin.varsecmod AS t2 USING (`Name`)
		WHERE t1.Public = 1 AND (LOWER(trim(t1.Name)) LIKE SUBSTRING_INDEX(theterm,' ',1));
	END IF; 
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `topicSearch`(IN seclevel INT(5), IN searchString VARCHAR(255))
    READS SQL DATA
    COMMENT 'Find matches for searchString in topic element of keywords table'
BEGIN
	DECLARE theterm VARCHAR(255);
	IF STRCMP(searchString,'*') = 0 THEN
		SET theterm = '%';
	ELSE
		SET theterm = searchString;
	END IF;
	SELECT t1.Name,t1.Label,t1.year,t1.Form,t1.QuestionNumber,t1.CardNumber,t1.Public FROM variablelabels as t1
    JOIN keywords as t2 ON (t1.Name = t2.Name)
	WHERE (getTopicFromKeywords(t2.term) = theterm) AND (seclevel + Public > 1 OR Public = 1)
	ORDER BY NAME;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `unembargoCardNumber`(IN cardnum CHARACTER VARYING(20))
    MODIFIES SQL DATA
    COMMENT 'Change the security level of all variables associated with the CardNumber - cardnum'
BEGIN
    DROP TEMPORARY TABLE IF EXISTS varstoembargo;
    CREATE TEMPORARY TABLE IF NOT EXISTS varstoembargo AS (SELECT NAME FROM variablelabels WHERE CardNumber = cardnum);
    SELECT CardNumber,COUNT(*) AS "Similar CardNumbers" FROM variablelabels GROUP BY CardNumber HAVING CardNumber LIKE cardnum;
    SELECT COUNT(*) AS "Variables to be unembargoed" FROM varstoembargo;
    DELETE FROM varsecmod WHERE Name IN (SELECT Name FROM varstoembargo);
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `updatequestnum`(IN varname CHARACTER VARYING (20), IN qnum CHARACTER VARYING (50))
    MODIFIES SQL DATA
    COMMENT 'This procedure updates the QuestionNumber field of matching variable varname'
BEGIN
	UPDATE variablelabels SET QuestionNumber = qnum WHERE NAME = varname;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `updatevarlabcode`(in varname character varying (20), IN newcode INTEGER (10), in newlabel character varying (255))
    MODIFIES SQL DATA
    COMMENT 'This procedure updates the Label and code fields of matching variable varname to be newlabel'
BEGIN
	update variablelabels set Label = newlabel where Name = varname;
	UPDATE categorymembers SET code = newcode where name = varname;
    END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `update_valuelabels`()
    COMMENT 'Updates valuelabels table with data from mapping_table'
begin
    -- First we need to remove valuelabels for everything that exists in the mapping_valuelabels table
    delete from valuelabels where Name in (select distinct Name from mapping_valuelabels);
    -- Now insert what we have from the new mapping file
    insert into valuelabels select   Name, value, new_meaning, MissingValueCode, 'IS', NOW(), 'upd_vals'  from mapping_valuelabels;
  end ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `update_variablelabels`()
    COMMENT 'Updates variablelabels table with data from mapping_table'
begin
    update variablelabels as t1 inner join mapping_table as t2 on (t1.Name = t2.Name)
       set
       t1.Label = t2.new_skylark_varlabel,
	   t1.printorder = t2.Orda,
	   t1.Derived = t2.Derived,
	   t1.Public = t2.pub,
	   t1.ReplaceWith = t2.ReplaceWith,
	   t1.Form = t2.Form,
	   t1.QuestionNumber = t2.QuestionNumber,
	   t1.YEAR = t2.yr,
	   t1.units = t2.units,
	   t1.field_id = t2.field_id,
	   t1.r_pub = t2.r_pub,
	   t1.r_sen = t2.r_sen,
	   t1.senstv = t2.senstv,
	   t1.notes = t2.notes;
  end ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `variables_year`(IN yearofinterest varchar(8))
    READS SQL DATA
    COMMENT 'Return the variable name and label of all data collected in a specified year'
BEGIN
SELECT name, label FROM variablelabels WHERE year = yearofinterest;
# we have changed this bit so the query runs on the year we have inputted 
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb3 */ ;
/*!50003 SET character_set_results = utf8mb3 */ ;
/*!50003 SET collation_connection  = utf8mb3_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `yearSearch`(IN seclevel INT(5), IN searchString VARCHAR(255))
    READS SQL DATA
    COMMENT 'Find matches for searchString in year field'
BEGIN
	DECLARE theterm VARCHAR(255);
	IF STRCMP(UPPER(searchString),'ALL') = 0 THEN
		SET theterm = '%';
	ELSE
		SET theterm = searchString;
	END IF;
	SELECT NAME,Label,Year,Form,QuestionNumber,CardNumber,Public FROM variablelabels
	WHERE (Year LIKE theterm) AND (seclevel + Public > 1 OR Public = 1)
	ORDER BY NAME;

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-01-31 19:09:26
