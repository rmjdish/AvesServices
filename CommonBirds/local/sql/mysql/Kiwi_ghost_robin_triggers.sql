-- MySQL dump 10.13  Distrib 8.0.41, for Linux (x86_64)
--
-- Host: mrc-kiwi01.ad.ucl.ac.uk    Database: ghost_robin
-- for installation on Kiwi in ghost_robin database only
-- Note although the users table is updated from Finch/robin
-- it is not necessary up add users to rook.users as new
-- baskets are always registered to the 'skylark' username
-- ------------------------------------------------------
-- Server version	8.0.31
DELIMITER ;;
CREATE DEFINER=`root`@`localhost`*/ TRIGGER `kiwi_ghost_basketdetails_insert_trigger` AFTER INSERT ON `basketdetails`
  FOR EACH ROW
    BEGIN
      insert into rook.basketdetails (basketID, Description, username, createDate)
      values (new.basketID, new.Description, 'skylark', new.createDate)
      on duplicate key update basketID = basketID;
    END;;
DELIMITER ;

CREATE DEFINER=`root`@`%`*/ TRIGGER `kiwi_ghost_basketdetails_update_trigger` AFTER UPDATE ON `basketdetails`
  FOR EACH ROW
    BEGIN
      UPDATE rook.basketdetails SET Description = new.Description,
				    username = new.username,
				    createDate = new.createDate
       WHERE basketID = new.basketID;
    END;;

CREATE DEFINER=`root`@`%`*/ TRIGGER `kiwi_ghost_shoppingbaskets_insert_trigger` AFTER INSERT ON `shoppingbaskets`
  FOR EACH ROW
    BEGIN
      INSERT INTO rook.shoppingbaskets (username, basketID, NAME)
      VALUES ('skylark', new.basketID, LOWER(new.name))
	ON DUPLICATE KEY UPDATE basketID = basketID;
    END;;

CREATE DEFINER=`root`@`%`*/ TRIGGER `kiwi_ghost_shoppingbaskets_update_trigger` AFTER UPDATE ON `shoppingbaskets`
  FOR EACH ROW
    BEGIN
      UPDATE rook.shoppingbaskets SET username = new.username,
				      name = lower(new.name)
       WHERE basketID = new.basketID;
    END;;
-- Dump completed on 2025-02-28 11:42:36
