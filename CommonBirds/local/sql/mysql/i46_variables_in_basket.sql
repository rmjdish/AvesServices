DELIMITER $$
use jay$$
drop function if exists i46_variables_in_basket$$

create function i46_variables_in_basket(basket VARCHAR(255))
returns boolean
reads sql data
language SQL
comment 'Returns a 1 if CardNumbers in a basket contain i46 variables'
begin
  declare result boolean;
  drop temporary table if exists temp_cards;
  create temporary table temp_cards  with
    get_Vars AS (select Name, CardNumber from rook.variablelabels),
    get_BasketVars AS (select BasketId, Name from rook.shoppingbaskets where BasketId = basket)
  select distinct TRIM(CardNumber) as 'CardNumber'
    from get_BasketVars left join get_Vars ON (get_BasketVars.Name = get_Vars.Name);
  -- Now we have a table with the list of CardNumbers for basket
   set @ncrds = 0;
   select count(t1.CardNumber) into @ncrds
     from temp_cards as t1
     where  t1.CardNumber IN (select t2.CardNumber from rook.filepath as t2 where t2.CardNumber like 'I46%');
  if @ncrds > 0 then
    set result = TRUE;
  else
    set result = FALSE;
  end if;
  return result;
end$$
DELIMITER ;
