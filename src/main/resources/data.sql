INSERT INTO usuarios (identificacion, nombre, apellido, telefono, correo, contrasena, activo)
SELECT '9000000001', 'Ana', 'Productora Demo', '3000000001', 'ana.demo@agrovalle.test',
       '$2b$10$uVngY.K.DpsVR5UMA7PzoudjFBt/yMalATYfyxF7SsmdJ7fyOLC7m', true
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE identificacion = '9000000001'
);

INSERT INTO usuarios (identificacion, nombre, apellido, telefono, correo, contrasena, activo)
SELECT '9000000002', 'Carlos', 'Productor Demo', '3000000002', 'carlos.demo@agrovalle.test',
       '$2b$10$uVngY.K.DpsVR5UMA7PzoudjFBt/yMalATYfyxF7SsmdJ7fyOLC7m', true
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE identificacion = '9000000002'
);

INSERT INTO usuarios (identificacion, nombre, apellido, telefono, correo, contrasena, activo)
SELECT '9000000003', 'Maria', 'Productora Demo', '3000000003', 'maria.demo@agrovalle.test',
       '$2b$10$uVngY.K.DpsVR5UMA7PzoudjFBt/yMalATYfyxF7SsmdJ7fyOLC7m', true
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE identificacion = '9000000003'
);

-- Rol de cada usuario demo (tabla asociativa roles_usuario: un usuario
-- puede tener varias filas, una por cada rol que ejerce).
INSERT INTO roles_usuario (usuario_id, rol)
SELECT usuario.id, 'AGRICULTOR'
FROM usuarios usuario
WHERE usuario.identificacion IN ('9000000001', '9000000002', '9000000003')
  AND NOT EXISTS (
      SELECT 1 FROM roles_usuario ru
      WHERE ru.usuario_id = usuario.id AND ru.rol = 'AGRICULTOR'
  );

-- Perfiles de agricultor (solo datos propios del rol), enlazados a su usuario por usuario_id.
INSERT INTO agricultores (municipio, usuario_id)
SELECT 'Palmira', usuario.id
FROM usuarios usuario
WHERE usuario.identificacion = '9000000001'
  AND NOT EXISTS (
      SELECT 1 FROM agricultores a WHERE a.usuario_id = usuario.id
  );

INSERT INTO agricultores (municipio, usuario_id)
SELECT 'Cali', usuario.id
FROM usuarios usuario
WHERE usuario.identificacion = '9000000002'
  AND NOT EXISTS (
      SELECT 1 FROM agricultores a WHERE a.usuario_id = usuario.id
  );

INSERT INTO agricultores (municipio, usuario_id)
SELECT 'Tuluá', usuario.id
FROM usuarios usuario
WHERE usuario.identificacion = '9000000003'
  AND NOT EXISTS (
      SELECT 1 FROM agricultores a WHERE a.usuario_id = usuario.id
  );

INSERT INTO productos (nombre, categoria, cantidad, unidad_medida, precio, fecha_cosecha, agricultor_id)
SELECT 'Aguacate Hass', 'Frutas', 50, 'KILOGRAMO', 3500, CURRENT_DATE + 7, agricultor.id
FROM agricultores agricultor
JOIN usuarios usuario ON usuario.id = agricultor.usuario_id
WHERE usuario.identificacion = '9000000001'
  AND NOT EXISTS (
      SELECT 1 FROM productos producto
      WHERE producto.nombre = 'Aguacate Hass'
        AND producto.agricultor_id = agricultor.id
  );

INSERT INTO productos (nombre, categoria, cantidad, unidad_medida, precio, fecha_cosecha, agricultor_id)
SELECT 'Tomate Chonto', 'Verduras', 0, 'LIBRA', 2800, CURRENT_DATE + 7, agricultor.id
FROM agricultores agricultor
JOIN usuarios usuario ON usuario.id = agricultor.usuario_id
WHERE usuario.identificacion = '9000000001'
  AND NOT EXISTS (
      SELECT 1 FROM productos producto
      WHERE producto.nombre = 'Tomate Chonto'
        AND producto.agricultor_id = agricultor.id
  );

INSERT INTO productos (nombre, categoria, cantidad, unidad_medida, precio, fecha_cosecha, agricultor_id)
SELECT 'Banano Cavendish', 'Frutas', 30, 'CAJA', 2200, CURRENT_DATE + 7, agricultor.id
FROM agricultores agricultor
JOIN usuarios usuario ON usuario.id = agricultor.usuario_id
WHERE usuario.identificacion = '9000000002'
  AND NOT EXISTS (
      SELECT 1 FROM productos producto
      WHERE producto.nombre = 'Banano Cavendish'
        AND producto.agricultor_id = agricultor.id
  );

INSERT INTO productos (nombre, categoria, cantidad, unidad_medida, precio, fecha_cosecha, agricultor_id)
SELECT 'Yuca Amarilla', 'Tubérculos', 20, 'BULTO', 1800, CURRENT_DATE + 7, agricultor.id
FROM agricultores agricultor
JOIN usuarios usuario ON usuario.id = agricultor.usuario_id
WHERE usuario.identificacion = '9000000003'
  AND NOT EXISTS (
      SELECT 1 FROM productos producto
      WHERE producto.nombre = 'Yuca Amarilla'
        AND producto.agricultor_id = agricultor.id
  );
INSERT INTO usuarios (identificacion, nombre, apellido, telefono, correo, contrasena, activo)
SELECT '9000000011', 'Carlos', 'Transportista Demo', '3000000011', 'transportista.demo@agrovalle.test',
       '$2b$10$uVngY.K.DpsVR5UMA7PzoudjFBt/yMalATYfyxF7SsmdJ7fyOLC7m', true
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE identificacion = '9000000011');

INSERT INTO usuarios (identificacion, nombre, apellido, telefono, correo, contrasena, activo)
SELECT '9000000012', 'Sofia', 'Comprador Cali', '3000000012', 'comprador.cali@agrovalle.test',
       '$2b$10$uVngY.K.DpsVR5UMA7PzoudjFBt/yMalATYfyxF7SsmdJ7fyOLC7m', true
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE identificacion = '9000000012');

INSERT INTO usuarios (identificacion, nombre, apellido, telefono, correo, contrasena, activo)
SELECT '9000000013', 'Luis', 'Comprador Palmira', '3000000013', 'comprador.palmira@agrovalle.test',
       '$2b$10$uVngY.K.DpsVR5UMA7PzoudjFBt/yMalATYfyxF7SsmdJ7fyOLC7m', true
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE identificacion = '9000000013');

INSERT INTO usuarios (identificacion, nombre, apellido, telefono, correo, contrasena, activo)
SELECT '9000000014', 'Ana', 'Comprador Jamundi', '3000000014', 'comprador.jamundi@agrovalle.test',
       '$2b$10$uVngY.K.DpsVR5UMA7PzoudjFBt/yMalATYfyxF7SsmdJ7fyOLC7m', true
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE identificacion = '9000000014');

INSERT INTO usuarios (identificacion, nombre, apellido, telefono, correo, contrasena, activo)
SELECT '9000000015', 'Pedro', 'Comprador Yumbo', '3000000015', 'comprador.yumbo@agrovalle.test',
       '$2b$10$uVngY.K.DpsVR5UMA7PzoudjFBt/yMalATYfyxF7SsmdJ7fyOLC7m', true
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE identificacion = '9000000015');

INSERT INTO roles_usuario (usuario_id, rol)
SELECT usuario.id, 'TRANSPORTISTA'
FROM usuarios usuario
WHERE usuario.identificacion = '9000000011'
  AND NOT EXISTS (
      SELECT 1 FROM roles_usuario ru WHERE ru.usuario_id = usuario.id AND ru.rol = 'TRANSPORTISTA'
  );

INSERT INTO roles_usuario (usuario_id, rol)
SELECT usuario.id, 'COMPRADOR'
FROM usuarios usuario
WHERE usuario.identificacion IN ('9000000012', '9000000013', '9000000014', '9000000015')
  AND NOT EXISTS (
      SELECT 1 FROM roles_usuario ru WHERE ru.usuario_id = usuario.id AND ru.rol = 'COMPRADOR'
  );

INSERT INTO transportistas (municipio, usuario_id)
SELECT 'Cali', usuario.id
FROM usuarios usuario
WHERE usuario.identificacion = '9000000011'
  AND NOT EXISTS (SELECT 1 FROM transportistas t WHERE t.usuario_id = usuario.id);

INSERT INTO compradores (municipio, direccion_entrega, latitud, longitud, usuario_id)
SELECT 'Cali', 'Calle 5 # 10-20', 3.451647, -76.531985, usuario.id
FROM usuarios usuario
WHERE usuario.identificacion = '9000000012'
  AND NOT EXISTS (SELECT 1 FROM compradores c WHERE c.usuario_id = usuario.id);

INSERT INTO compradores (municipio, direccion_entrega, latitud, longitud, usuario_id)
SELECT 'Palmira', 'Carrera 28 # 30-15', 3.539400, -76.303600, usuario.id
FROM usuarios usuario
WHERE usuario.identificacion = '9000000013'
  AND NOT EXISTS (SELECT 1 FROM compradores c WHERE c.usuario_id = usuario.id);

INSERT INTO compradores (municipio, direccion_entrega, latitud, longitud, usuario_id)
SELECT 'Jamundi', 'Calle 12 # 8-40', NULL, NULL, usuario.id
FROM usuarios usuario
WHERE usuario.identificacion = '9000000014'
  AND NOT EXISTS (SELECT 1 FROM compradores c WHERE c.usuario_id = usuario.id);

INSERT INTO compradores (municipio, direccion_entrega, latitud, longitud, usuario_id)
SELECT 'Yumbo', 'Carrera 5 # 7-12', 3.585300, -76.495300, usuario.id
FROM usuarios usuario
WHERE usuario.identificacion = '9000000015'
  AND NOT EXISTS (SELECT 1 FROM compradores c WHERE c.usuario_id = usuario.id);

INSERT INTO vehiculos (tipo_transporte, placa, capacidad_kg, transportista_id)
SELECT 'CAMIONETA', 'DEM123', 800.00, transportista.id
FROM transportistas transportista
JOIN usuarios usuario ON usuario.id = transportista.usuario_id
WHERE usuario.identificacion = '9000000011'
  AND NOT EXISTS (SELECT 1 FROM vehiculos v WHERE v.placa = 'DEM123');

INSERT INTO rutas (fecha, vehiculo_id)
SELECT CURRENT_DATE, vehiculo.id
FROM vehiculos vehiculo
WHERE vehiculo.placa = 'DEM123'
  AND NOT EXISTS (SELECT 1 FROM rutas r WHERE r.vehiculo_id = vehiculo.id);

INSERT INTO pedidos (estado, direccion_entrega, fecha_creacion, fecha_estimada_llegada,
                     comprador_id, agricultor_id)
SELECT 'EN_TRANSPORTE', comprador.direccion_entrega, CURRENT_TIMESTAMP, CURRENT_DATE,
       comprador.id, agricultor.id
FROM compradores comprador
JOIN usuarios usuario_comprador ON usuario_comprador.id = comprador.usuario_id
JOIN usuarios usuario_agricultor ON usuario_agricultor.identificacion = '9000000001'
JOIN agricultores agricultor ON agricultor.usuario_id = usuario_agricultor.id
WHERE usuario_comprador.identificacion IN ('9000000012', '9000000013', '9000000014', '9000000015')
  AND NOT EXISTS (SELECT 1 FROM pedidos p WHERE p.comprador_id = comprador.id);

INSERT INTO asignaciones_transporte (pedido_id, ruta_id, orden_entrega, fecha_asignacion)
SELECT pedido.id, ruta.id,
       CASE usuario.identificacion
         WHEN '9000000012' THEN 1
         WHEN '9000000013' THEN 2
         WHEN '9000000014' THEN 3
         ELSE 4
       END,
       CURRENT_TIMESTAMP
FROM pedidos pedido
JOIN compradores comprador ON comprador.id = pedido.comprador_id
JOIN usuarios usuario ON usuario.id = comprador.usuario_id
JOIN vehiculos vehiculo ON vehiculo.placa = 'DEM123'
JOIN rutas ruta ON ruta.vehiculo_id = vehiculo.id
WHERE usuario.identificacion IN ('9000000012', '9000000013', '9000000014', '9000000015')
  AND NOT EXISTS (SELECT 1 FROM asignaciones_transporte a WHERE a.pedido_id = pedido.id);