-- Phil Curran February 2024
-- SQL Script to update robin/rook databases with extra fields
DELIMITER $$

ALTER TABLE shadowvariables
	ADD COLUMN senstv tinyint(4),
	ADD COLUMN r_pub VARCHAR(128),
	ADD COLUMN r_sen VARCHAR(129),
    ADD COLUMN notes VARCHAR(512) 
    $$
      
ALTER TABLE variablelabels
	ADD COLUMN senstv tinyint(4),
	ADD COLUMN r_pub VARCHAR(128),
	ADD COLUMN r_sen VARCHAR(129),
    ADD COLUMN notes VARCHAR(512) 
    $$


DELIMITER ;