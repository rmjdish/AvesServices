DELIMITER $$

CREATE DATABASE IF NOT EXISTS `audit`$$

USE `audit`$$

CREATE TABLE `history` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `tstamp` datetime NOT NULL,
  `operation` varchar(20) NOT NULL,
  `db` varchar(20) NOT NULL,
  `tab` varchar(20) NOT NULL,
  `fld` varchar(20) NOT NULL,
  `old_value` varchar(255) NOT NULL,
  `new_value` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
)$$

DELIMITER ;


