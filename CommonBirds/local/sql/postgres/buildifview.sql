
CREATE OR REPLACE PROCEDURE buildifview ()
  LANGUAGE 'plpgsql'
AS $function1$
BEGIN
  -- View: public.icol_frequencies
  CREATE OR REPLACE VIEW public.icol_frequencies
   AS
   SELECT icol_types.table_schema::character varying,
      icol_types.table_name::character varying,
      frequencies.fieldname::character varying,
      frequencies.fieldvalue::bigint,
      frequencies.fieldcount::integer,
      icol_types.data_type::character varying,
      icol_types.isnominal::boolean
     FROM frequencies
       LEFT JOIN icol_types ON icol_types.column_name::text = frequencies.fieldname::text;

  ALTER TABLE public.icol_frequencies
      OWNER TO postgres;
  COMMENT ON VIEW public.icol_frequencies
      IS 'This is a combination of the information in the icol_types and frequencies tables';
END$function1$
