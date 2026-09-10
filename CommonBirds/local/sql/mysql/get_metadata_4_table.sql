-- Phil Curran February 2024, updated November 2024
-- MySQL Proc to return metadata for a table in PG
-- Needs table in MySQL that records any discrepencies between CardNumbers and table names
DELIMITER $$
USE `robin`$$

DROP PROCEDURE IF EXISTS `get_metadata_4_table`$$

CREATE PROCEDURE `get_metadata_4_table`(pgtable VARCHAR(128))
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
END$$
DELIMITER ;

