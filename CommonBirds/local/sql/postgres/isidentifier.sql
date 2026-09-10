CREATE OR REPLACE FUNCTION public.isidentifer(varname character varying)
 RETURNS boolean
 LANGUAGE plpgsql
AS $function$
DECLARE
    gotident boolean := FALSE;
    nrows integer := 0;
    nproc integer :=0;
BEGIN
  SELECT varname IN (SELECT value from public.identifiers) INTO gotident;
  RETURN gotident;
  END; 

$function$
