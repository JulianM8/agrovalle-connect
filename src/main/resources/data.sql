INSERT INTO usuarios (identificacion, correo, contrasena, rol, activo)
SELECT '9000000001', 'ana.demo@agrovalle.test',
       '$2b$10$uVngY.K.DpsVR5UMA7PzoudjFBt/yMalATYfyxF7SsmdJ7fyOLC7m', 'AGRICULTOR', true
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE identificacion = '9000000001'
);

INSERT INTO usuarios (identificacion, correo, contrasena, rol, activo)
SELECT '9000000002', 'carlos.demo@agrovalle.test',
       '$2b$10$uVngY.K.DpsVR5UMA7PzoudjFBt/yMalATYfyxF7SsmdJ7fyOLC7m', 'AGRICULTOR', true
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE identificacion = '9000000002'
);

INSERT INTO usuarios (identificacion, correo, contrasena, rol, activo)
SELECT '9000000003', 'maria.demo@agrovalle.test',
       '$2b$10$uVngY.K.DpsVR5UMA7PzoudjFBt/yMalATYfyxF7SsmdJ7fyOLC7m', 'AGRICULTOR', true
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE identificacion = '9000000003'
);

-- Perfiles de agricultor, enlazados a su usuario por usuario_id.
INSERT INTO agricultores (nombre, apellido, municipio, telefono, usuario_id)
SELECT 'Ana', 'Productora Demo', 'Palmira', '3000000001', usuario.id
FROM usuarios usuario
WHERE usuario.identificacion = '9000000001'
  AND NOT EXISTS (
      SELECT 1 FROM agricultores a WHERE a.usuario_id = usuario.id
  );

INSERT INTO agricultores (nombre, apellido, municipio, telefono, usuario_id)
SELECT 'Carlos', 'Productor Demo', 'Cali', '3000000002', usuario.id
FROM usuarios usuario
WHERE usuario.identificacion = '9000000002'
  AND NOT EXISTS (
      SELECT 1 FROM agricultores a WHERE a.usuario_id = usuario.id
  );

INSERT INTO agricultores (nombre, apellido, municipio, telefono, usuario_id)
SELECT 'Maria', 'Productora Demo', 'Tuluá', '3000000003', usuario.id
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