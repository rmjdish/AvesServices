DELIMITER $$
CREATE DEFINER=`root`@`localhost` PROCEDURE `checkout`(IN pd VARCHAR(16), IN bd VARCHAR(120))
    COMMENT 'Takes a Project ID (pd) and a Basket ID (bd) and inserts these values into the datasets table in jay.  No dups created'
BEGIN
	if not exists(select 1 from `jay`.`datasets` where pid=pd and basketid=bd limit 1) then
		insert into `jay`.`datasets` (basketid,pid,datesent) values (bd,pd,DATE(now()));
	end if;
END $$
DELIMITER ;
