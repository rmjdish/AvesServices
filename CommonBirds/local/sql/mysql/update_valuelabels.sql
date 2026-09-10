-- SQL Procedure to update the valuelabels table from mapping_valuelabels
-- Note does not update CardNumber as this could leave variables missing in action!
DELIMITER $$

drop procedure if exists update_valuelabels$$

create procedure update_valuelabels()
  COMMENT 'Updates valuelabels table with data from mapping_table'
  begin
    -- First we need to remove valuelabels for everything that exists in the mapping_valuelabels table
    delete from valuelabels where Name in (select distinct Name from mapping_valuelabels);
    -- Now insert what we have from the new mapping file
    insert into valuelabels select   Name, value, new_meaning, MissingValueCode, 'IS', NOW(), 'update_valuelabels.sql'  from mapping_valuelabels;
  end$$
DELIMITER ;

    
