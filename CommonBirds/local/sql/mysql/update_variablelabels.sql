-- SQL Procedure to update the variablelabels table from mapping_table
-- Note does not update CardNumber as this could leave variables missing in action!
DELIMITER $$

drop procedure if exists update_variablelabels$$

create procedure update_variablelabels()
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
  end$$
DELIMITER ;

    
