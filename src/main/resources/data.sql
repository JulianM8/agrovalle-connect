INSERT INTO agricultores (nombre, apellido, identificacion, municipio, telefono, correo)
SELECT 'Ana', 'Productora Demo', '900000001', 'Palmira', '3000000001',
       'ana.demo@agrovalle.test'
WHERE NOT EXISTS (
    SELECT 1 FROM agricultores WHERE identificacion = '900000001'
);

INSERT INTO agricultores (nombre, apellido, identificacion, municipio, telefono, correo)
SELECT 'Carlos', 'Productor Demo', '900000002', 'Cali', '3000000002',
       'carlos.demo@agrovalle.test'
WHERE NOT EXISTS (
    SELECT 1 FROM agricultores WHERE identificacion = '900000002'
);

INSERT INTO agricultores (nombre, apellido, identificacion, municipio, telefono, correo)
SELECT 'Maria', 'Productora Demo', '900000003', 'Tuluá', '3000000003',
       'maria.demo@agrovalle.test'
WHERE NOT EXISTS (
    SELECT 1 FROM agricultores WHERE identificacion = '900000003'
);

INSERT INTO productos (nombre, categoria, cantidad, precio, fecha_cosecha, agricultor_id)
SELECT 'Aguacate Hass', 'Frutas', 50, 3500, CURRENT_DATE + 7, agricultor.id
FROM agricultores agricultor
WHERE agricultor.identificacion = '900000001'
  AND NOT EXISTS (
      SELECT 1 FROM productos producto
      WHERE producto.nombre = 'Aguacate Hass'
        AND producto.agricultor_id = agricultor.id
  );

INSERT INTO productos (nombre, categoria, cantidad, precio, fecha_cosecha, agricultor_id)
SELECT 'Tomate Chonto', 'Verduras', 0, 2800, CURRENT_DATE + 7, agricultor.id
FROM agricultores agricultor
WHERE agricultor.identificacion = '900000001'
  AND NOT EXISTS (
      SELECT 1 FROM productos producto
      WHERE producto.nombre = 'Tomate Chonto'
        AND producto.agricultor_id = agricultor.id
  );

INSERT INTO productos (nombre, categoria, cantidad, precio, fecha_cosecha, agricultor_id)
SELECT 'Banano Cavendish', 'Frutas', 30, 2200, CURRENT_DATE + 7, agricultor.id
FROM agricultores agricultor
WHERE agricultor.identificacion = '900000002'
  AND NOT EXISTS (
      SELECT 1 FROM productos producto
      WHERE producto.nombre = 'Banano Cavendish'
        AND producto.agricultor_id = agricultor.id
  );

INSERT INTO productos (nombre, categoria, cantidad, precio, fecha_cosecha, agricultor_id)
SELECT 'Yuca Amarilla', 'Tubérculos', 20, 1800, CURRENT_DATE + 7, agricultor.id
FROM agricultores agricultor
WHERE agricultor.identificacion = '900000003'
  AND NOT EXISTS (
      SELECT 1 FROM productos producto
      WHERE producto.nombre = 'Yuca Amarilla'
        AND producto.agricultor_id = agricultor.id
  );
