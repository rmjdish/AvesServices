DELIMITER $$

drop procedure if exists cardnumbers_in_basket$$

create procedure cardnumbers_in_basket(basket VARCHAR(255))
language SQL
comment 'Returns a 1 field table of all CardNumbers in a basket'
begin
  with
    get_Vars AS (select Name, CardNumber from variablelabels),
    get_BasketVars AS (select BasketId, Name from shoppingbaskets where BasketId = basket)
  select distinct TRIM(CardNumber) from get_BasketVars left join get_Vars ON (get_BasketVars.Name = get_Vars.Name);
end$$
DELIMITER ;
