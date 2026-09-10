-- Phil Curran January 2024
DELIMITER $$
DROP VIEW IF EXISTS `var_meta_combo`$$
CREATE VIEW `var_meta_combo` AS (
select
  `t1`.`Name`             AS `Name`,
  `t1`.`Value`            AS `Value`,
  `t1`.`Label`            AS `Label`,
  `t1`.`MissingValueCode` AS `MissingValueCode`,
  `t2`.`CardNumber`       AS `CardNumber`,
  `t2`.`Label`            AS `VarLabel`,
  `t2`.`Public`           AS `Public`,
  `t2`.`units`            AS `units`
from (`valuelabels` `t1`
   left join `variablelabels` `t2`
     on (`t1`.`Name` = `t2`.`Name`)))$$
DELIMITER ;     
