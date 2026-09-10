DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `completed_project_search`(IN datefrom date, in dateto date)
    READS SQL DATA
    COMMENT 'Return list of projects finished between certain dates'
BEGIN
	SET datefrom = IFNULL(datefrom, '2018-08-01');
	SET dateto = IFNULL(dateto, curdate());
	select * from finished_projects where `data sent out` between datefrom and dateto;
END ;;
DELIMITER ;
