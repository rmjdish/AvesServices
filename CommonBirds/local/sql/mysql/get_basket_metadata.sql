-- MySQL procedure to return metadata for variables in an existing basket
-- This does not return metadata for serno
-- Phil Curran May 2025
DELIMITER $$
use rook$$

drop procedure if exists get_basket_varmeta$$

create procedure get_basket_varmeta(basket VARCHAR(255))
reads sql data
language SQL
comment 'return variable metadata for variables in an existing basket'
begin
  drop temporary table if exists bsk_varmeta;
  -- Using CTE to create table
  create temporary table bsk_varmeta as
  with
    get_BasketVars AS (select Name from rook.shoppingbaskets where basketId = basket and Name NOT LIKE 'serno' and Name NOT LIKE '%.%')
  select
    Name,
    Label
  from rook.variablelabels where Name in (select Name from get_BasketVars);
  select * from bsk_varmeta;
end$$

drop procedure if exists get_basket_valmeta$$

create procedure get_basket_valmeta(basket VARCHAR(255))
reads sql data
language SQL
comment 'return value labels metadata for variables in an existing basket'
begin
  drop temporary table if exists bsk_valmeta;
  -- Using CTE to create table
  create temporary table bsk_valmeta as
  with
    get_BasketVars AS (select Name from rook.shoppingbaskets where basketId = basket and Name NOT LIKE 'serno' and Name NOT LIKE '%.%')
  select
    Name,
    Value,
    Label
  from rook.valuelabels where Name in (select Name from get_BasketVars);
  select * from bsk_valmeta;
end$$


DELIMITER ;
