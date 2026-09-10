-- Used to check whether a basket contains specific types of data
-- Phil Curran Januyary 2024
DELIMITER $$
use jay$$
drop procedure if exists i46_basket_form_check$$

create procedure i46_basket_form_check(basket VARCHAR(255))
reads sql data
language SQL
comment 'Returns a 1 row table of basket/form consistency checks on a basket'
begin
SELECT t1.basketid, 
	t1.pid, 
	t3.prinapp, 
	t3.prinappemail, 
	t3.title,
	i46_variables_in_basket(t1.basketid) AS 'Has_I46_Vars', 
	t3.insight46, 
	t3.imagingdata, 
	t3.dna, 
	t3.samples, 
	t3.rna 
FROM jay.linkedbaskets AS t1 
LEFT JOIN jay.projects AS t2 ON (t1.pid = t2.pid)
LEFT JOIN jay.forms    AS t3 ON (t2.formid = t3.formid)
WHERE (t1.basketid = basket);
end$$
DELIMITER ;
