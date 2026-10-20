-- =====================================================================
-- VetGest - Pruebas de integridad (cada sentencia DEBE fallar)
-- Ejecutar con: mysql --force -u root < 05_pruebas_integridad.sql
-- =====================================================================
USE vetgest_db;

-- I1. Turno superpuesto para el veterinario 1 (lo bloquea el trigger)
INSERT INTO turno (id_mascota, id_veterinario, id_sucursal, id_usuario, fecha_hora, motivo)
VALUES (2, 1, 1, 1, '2026-09-28 09:15:00', 'Prueba superposicion');

-- I2. Borrar un cliente que tiene mascotas (FK ON DELETE RESTRICT)
DELETE FROM cliente WHERE id_cliente = 1;

-- I3. Dejar stock negativo en un lote (CHECK ck_lote_cantidad)
UPDATE lote_insumo SET cantidad_actual = cantidad_actual - 100 WHERE id_lote = 3;

-- I4. Segunda historia clinica para el mismo turno (UNIQUE uq_historia_turno)
INSERT INTO historia_clinica (id_turno, peso_kg, diagnostico, tratamiento)
VALUES (1, 6.5, 'Duplicado', 'Duplicado');

-- I5. Consumo sin historia clinica asociada (CHECK ck_mov_consumo)
INSERT INTO mov_inventario (id_lote, id_historia, id_usuario, tipo_mov, cantidad)
VALUES (1, NULL, 3, 'CONSUMO', 1);
