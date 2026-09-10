  -- View: public.icol_frequencies

  -- DROP VIEW public.icol_frequencies;

  CREATE OR REPLACE VIEW public.icol_frequencies
   AS
   SELECT icol_types.table_schema,
      icol_types.table_name,
      frequencies.fieldname,
      frequencies.fieldvalue,
      frequencies.fieldcount,
      icol_types.data_type,
      icol_types.isnominal
     FROM frequencies
       LEFT JOIN icol_types ON icol_types.column_name::text = frequencies.fieldname::text;

  ALTER TABLE public.icol_frequencies
      OWNER TO postgres;
  COMMENT ON VIEW public.icol_frequencies
      IS 'This is a combination of the information in the icol_types and frequencies tables';
