/*!40101 SET NAMES utf8 */;
/*!40101 SET SQL_MODE=''*/;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

-- These triggers are for installation on Finch into the robin database ONLY!! 

USE `robin`;

/* Trigger structure for table `basketdetails` in robin database */

DELIMITER $$

CREATE DEFINER = 'root'@'%' TRIGGER `ghost_basketdetails` AFTER INSERT ON `basketdetails`
FOR EACH ROW
    BEGIN
	INSERT INTO ghost_robin.basketdetails VALUES (new.basketID, new.Description, new.username, new.createDate)
	ON DUPLICATE KEY UPDATE basketID = basketID;
    END $$


CREATE DEFINER = 'root'@'%' TRIGGER `ghost_basketdetails_update` AFTER UPDATE ON `basketdetails`
FOR EACH ROW
    BEGIN
	UPDATE ghost_robin.basketdetails SET Description = new.Description,
					     username = new.username,
					     createDate = new.createDate
	 WHERE basketID = new.basketID;
    END $$

CREATE DEFINER = 'root'@'%' TRIGGER `ghost_shoppingbaskets_insert_trigger` AFTER INSERT ON `shoppingbaskets`
FOR EACH ROW
    BEGIN
	INSERT INTO ghost_robin.shoppingbaskets (username, basketID, NAME) VALUES (new.username, new.basketID, LOWER(new.name))
	ON DUPLICATE KEY UPDATE basketID = basketID;
    END $$

CREATE DEFINER = 'root'@'%' TRIGGER `ghost_shoppingbaskets_update_trigger` AFTER UPDATE ON `shoppingbaskets`
FOR EACH ROW
    BEGIN
	UPDATE ghost_robin.shoppingbaskets SET username = new.username, NAME = LOWER(new.name)
	WHERE basketID = new.basketID;
    END $$

CREATE DEFINER = 'root'@'%' TRIGGER `ghost_users_insert` AFTER INSERT ON `users`
FOR EACH ROW
    BEGIN
	INSERT INTO ghost_robin.users (username, firstName, lastName, affiliation,
					address1, address2, city, postcode, county,
					phone, email, password, status, reviewer, CreateDate) 
	VALUES (new.username, new.firstName, new.lastName, new.affiliation,
		new.address1, new.address2, new.city, new.postcode,
		new.county, new.phone, new.email, new.password, new.status, new.reviewer,
		new.CreateDate)
	ON DUPLICATE KEY UPDATE username = username;
    END $$

/* Trigger structure for table `users` in robin database */

CREATE DEFINER = 'root'@'%' TRIGGER `ghost_users_update` AFTER UPDATE ON `users`
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


DELIMITER ;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
