-- ALTER TABLE para agregar contraseña a CLIENTES
ALTER TABLE CLIENTES ADD password VARCHAR2(128);
-- Opcional: Si quieres forzar que siempre tenga contraseña, puedes poner NOT NULL (solo si no hay clientes existentes)
-- ALTER TABLE CLIENTES MODIFY password VARCHAR2(128) NOT NULL;