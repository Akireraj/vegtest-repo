-- =====================================================================
-- VetGest - Actualizacion (UPDATE) y borrado (DELETE) de registros
-- =====================================================================
USE vetgest_db;

-- U1. Confirmar un turno reservado (RF03)
UPDATE turno SET estado = 'CONFIRMADO'
 WHERE id_turno = 9 AND estado = 'RESERVADO';

-- U2. Reprogramar un turno a un horario libre (RF03)
UPDATE turno SET fecha_hora = '2026-09-29 11:00:00'
 WHERE id_turno = 10 AND estado IN ('RESERVADO','CONFIRMADO');

-- U3. Registrar una reposicion de stock en forma transaccional (RF07)
START TRANSACTION;
  INSERT INTO mov_inventario (id_lote, id_historia, id_usuario, tipo_mov, cantidad)
  VALUES (2, NULL, 6, 'REPOSICION', 20);
  UPDATE lote_insumo SET cantidad_actual = cantidad_actual + 20 WHERE id_lote = 2;
COMMIT;

-- U4. Baja logica de una mascota (se conserva su historia clinica)
UPDATE mascota SET activa = FALSE WHERE id_mascota = 6;

SELECT id_turno, fecha_hora, estado FROM turno WHERE id_turno IN (9, 10);
SELECT sucursal, insumo, stock_vigente, stock_minimo
  FROM v_stock_sucursal WHERE id_sucursal = 2 AND id_insumo = 1;

-- D1. Borrado fisico de un turno cancelado que no genero historia clinica
DELETE FROM turno
 WHERE id_turno = 6 AND estado = 'CANCELADO'
   AND NOT EXISTS (SELECT 1 FROM historia_clinica h WHERE h.id_turno = 6);

-- D2. Alta y borrado de un cliente cargado por error (sin mascotas asociadas)
INSERT INTO cliente (dni, nombre, apellido, telefono) VALUES ('99999999','Prueba','Borrar','000');
DELETE FROM cliente WHERE dni = '99999999';

SELECT COUNT(*) AS turnos_restantes FROM turno;
SELECT COUNT(*) AS clientes FROM cliente;
