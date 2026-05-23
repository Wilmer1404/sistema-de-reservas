-- ============================================================
-- 04_datos_iniciales.sql
-- Sistema de Reservas SpaceWork Perú S.A.C.
-- Datos de inicialización: roles, usuarios, espacios, descuentos
-- ============================================================

-- ROLES
INSERT INTO ROLES (id_rol, nombre, descripcion, permisos) VALUES
  (seq_rol.NEXTVAL, 'ADMIN', 'Administrador total',
   '["USUARIO_*","ESPACIO_*","RESERVA_*","PAGO_*","DESCUENTO_*","AUDITORIA_READ"]');

INSERT INTO ROLES (id_rol, nombre, descripcion, permisos) VALUES
  (seq_rol.NEXTVAL, 'RECEPCIONISTA', 'Gestiona reservas y pagos',
   '["RESERVA_READ","RESERVA_CREATE","PAGO_READ","PAGO_UPDATE"]');

INSERT INTO ROLES (id_rol, nombre, descripcion, permisos) VALUES
  (seq_rol.NEXTVAL, 'GERENTE', 'Consulta y reportes',
   '["RESERVA_READ","PAGO_READ","DESCUENTO_READ","AUDITORIA_READ"]');

-- USUARIOS INICIALES
-- Password: Admin123! (se reemplaza con hash SHA-256 en la app)
-- Salt: saltadmin
INSERT INTO USUARIOS (id_usuario, username, password, salt, nombre, email, id_rol) VALUES
  (seq_usuario.NEXTVAL, 'admin',
   '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918',
   'saltadmin',
   'Administrador', 'admin@spacework.com', 1);

INSERT INTO USUARIOS (id_usuario, username, password, salt, nombre, email, id_rol) VALUES
  (seq_usuario.NEXTVAL, 'recepcionista',
   '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918',
   'saltrecep',
   'Recepcionista', 'recepcionista@spacework.com', 2);

INSERT INTO USUARIOS (id_usuario, username, password, salt, nombre, email, id_rol) VALUES
  (seq_usuario.NEXTVAL, 'gerente',
   '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918',
   'saltgerente',
   'Gerente de Espacios', 'gerente@spacework.com', 3);

-- ESPACIOS
INSERT INTO ESPACIOS (id_espacio, nombre, tipo, capacidad, ubicacion, precio_por_hora, descripcion) VALUES
  (seq_espacio.NEXTVAL, 'Sala Premium', 'Sala de Conferencias', 20, 'Piso 5', 150.00,
   'Sala equipada con proyector 4K, pizarra interactiva y videoconferencia.');

INSERT INTO ESPACIOS (id_espacio, nombre, tipo, capacidad, ubicacion, precio_por_hora, descripcion) VALUES
  (seq_espacio.NEXTVAL, 'Auditorio Central', 'Auditorio', 100, 'Piso 1', 300.00,
   'Auditorio con escenario, sonido envolvente y butacas reclinables.');

INSERT INTO ESPACIOS (id_espacio, nombre, tipo, capacidad, ubicacion, precio_por_hora, descripcion) VALUES
  (seq_espacio.NEXTVAL, 'Oficina Compartida 3', 'Oficina', 6, 'Piso 3', 80.00,
   'Oficina para hasta 6 personas, con WiFi y café incluido.');

INSERT INTO ESPACIOS (id_espacio, nombre, tipo, capacidad, ubicacion, precio_por_hora, descripcion) VALUES
  (seq_espacio.NEXTVAL, 'Sala de Juntas Ejecutiva', 'Sala de Conferencias', 12, 'Piso 8', 200.00,
   'Sala ejecutiva con mesa de caoba, videoconferencia y pantallas LED.');

INSERT INTO ESPACIOS (id_espacio, nombre, tipo, capacidad, ubicacion, precio_por_hora, descripcion) VALUES
  (seq_espacio.NEXTVAL, 'Auditorio Pequeño', 'Auditorio', 40, 'Piso 2', 150.00,
   'Auditorio para eventos pequeños con sistema de sonido profesional.');

-- CLIENTES DE EJEMPLO
INSERT INTO CLIENTES (id_cliente, nombre, apellido, dni, email, telefono) VALUES
  (seq_cliente.NEXTVAL, 'Juan', 'Pérez', '12345678', 'juan@example.com', '987654321');

INSERT INTO CLIENTES (id_cliente, nombre, apellido, dni, email, telefono) VALUES
  (seq_cliente.NEXTVAL, 'María', 'González', '87654321', 'maria@example.com', '987654322');

INSERT INTO CLIENTES (id_cliente, nombre, apellido, dni, email, telefono) VALUES
  (seq_cliente.NEXTVAL, 'Carlos', 'López', '11111111', 'carlos@example.com', '987654323');

-- DESCUENTOS
INSERT INTO DESCUENTOS (id_descuento, codigo, descripcion, porcentaje, monto_minimo, 
                        usos_maximos, fecha_inicio, fecha_fin) VALUES
  (seq_descuento.NEXTVAL, 'BIENVENIDA10', 'Descuento de bienvenida 10%',
   10.0, 500.00, 100, SYSDATE, SYSDATE + 90);

INSERT INTO DESCUENTOS (id_descuento, codigo, descripcion, porcentaje, monto_minimo,
                        usos_maximos, fecha_inicio, fecha_fin) VALUES
  (seq_descuento.NEXTVAL, 'VERANO20', 'Promoción de verano 20%',
   20.0, 1000.00, 50, SYSDATE, SYSDATE + 60);

INSERT INTO DESCUENTOS (id_descuento, codigo, descripcion, porcentaje, monto_minimo,
                        usos_maximos, fecha_inicio, fecha_fin) VALUES
  (seq_descuento.NEXTVAL, 'CLIENTE15', 'Descuento para clientes regulares 15%',
   15.0, 800.00, 200, SYSDATE, SYSDATE + 180);

COMMIT;
