CREATE FUNCTION get_ndiscvals (pschema text, ptable text, pfield text)
  RETURNS integer
  LANGUAGE 'plpgsql'
  STRICT
AS $$
DECLARE
  ndiscvals integer := 0;
BEGIN
  EXECUTE format('select count(*) from (select %I,  count(*) from %I.%I group by %I ) as forgetme ',
		 pfield, pschema, ptable, pfield)
    INTO ndiscvals;
  RETURN ndiscvals;
END;
$$
