DO $$DECLARE sentencia_truncate RECORD;
generar_sentencias_truncate CURSOR FOR SELECT 'DROP SCHEMA ' || schema_name || ' CASCADE;' script FROM information_schema.schemata WHERE schema_name LIKE 'servicio%';
BEGIN
FOR sentencia_truncate in generar_sentencias_truncate LOOP
  execute sentencia_truncate.script;
END LOOP;
END$$;
