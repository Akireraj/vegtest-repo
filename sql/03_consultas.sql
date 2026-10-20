-- =====================================================================
-- VetGest - Consultas SQL (SELECT) asociadas a los requerimientos
-- =====================================================================
USE vetgest_db;

-- Q1. Agenda del dia de una sucursal (RF01 / RNF03)
SELECT t.id_turno, TIME(t.fecha_hora) AS hora, t.duracion_min AS min,
       CONCAT(v.apellido, ', ', v.nombre) AS veterinario,
       m.nombre AS mascota, CONCAT(c.apellido, ', ', c.nombre) AS cliente,
       t.motivo, t.estado
  FROM turno t
  JOIN veterinario v ON v.id_veterinario = t.id_veterinario
  JOIN mascota m     ON m.id_mascota     = t.id_mascota
  JOIN cliente c     ON c.id_cliente     = m.id_cliente
 WHERE t.id_sucursal = 1
   AND DATE(t.fecha_hora) = '2026-09-28'
 ORDER BY t.fecha_hora;

-- Q2. Historial clinico completo de una mascota, en todas las sucursales (RF05)
SELECT DATE(t.fecha_hora) AS fecha, s.nombre AS sucursal,
       CONCAT(v.apellido, ', ', v.nombre) AS veterinario,
       h.peso_kg, h.diagnostico, h.tratamiento
  FROM historia_clinica h
  JOIN turno t       ON t.id_turno       = h.id_turno
  JOIN sucursal s    ON s.id_sucursal    = t.id_sucursal
  JOIN veterinario v ON v.id_veterinario = t.id_veterinario
 WHERE t.id_mascota = 1
 ORDER BY t.fecha_hora DESC;

-- Q3. Deteccion de superposicion: la misma consulta que ejecuta TurnoDAO (RF02)
--     Parametros: veterinario 1, inicio 2026-09-28 09:15, duracion 30 min
SELECT COUNT(*) AS turnos_en_conflicto
  FROM turno t
 WHERE t.id_veterinario = 1
   AND t.estado IN ('RESERVADO','CONFIRMADO')
   AND '2026-09-28 09:15:00' < DATE_ADD(t.fecha_hora, INTERVAL t.duracion_min MINUTE)
   AND t.fecha_hora < DATE_ADD('2026-09-28 09:15:00', INTERVAL 30 MINUTE);

-- Q4. Reporte de turnos por sucursal y estado en un periodo (RF09)
SELECT s.nombre AS sucursal,
       SUM(t.estado = 'ATENDIDO')  AS atendidos,
       SUM(t.estado = 'AUSENTE')   AS ausentes,
       SUM(t.estado = 'CANCELADO') AS cancelados,
       SUM(t.estado IN ('RESERVADO','CONFIRMADO')) AS pendientes,
       COUNT(*) AS total
  FROM sucursal s
  LEFT JOIN turno t ON t.id_sucursal = s.id_sucursal
                   AND t.fecha_hora BETWEEN '2026-09-01' AND '2026-09-30 23:59:59'
 GROUP BY s.id_sucursal, s.nombre
 ORDER BY s.id_sucursal;

-- Q5. Insumos consumidos por sucursal en el periodo (RF09)
SELECT s.nombre AS sucursal, i.nombre AS insumo, SUM(mv.cantidad) AS consumido, i.unidad
  FROM mov_inventario mv
  JOIN lote_insumo l ON l.id_lote     = mv.id_lote
  JOIN insumo i      ON i.id_insumo   = l.id_insumo
  JOIN sucursal s    ON s.id_sucursal = l.id_sucursal
 WHERE mv.tipo_mov = 'CONSUMO'
   AND mv.fecha BETWEEN '2026-09-01' AND '2026-09-30 23:59:59'
 GROUP BY s.nombre, i.nombre, i.unidad
 ORDER BY s.nombre, consumido DESC;

-- Q6. Alertas de stock minimo por sucursal (RF08)
SELECT sucursal, codigo, insumo, stock_vigente, stock_minimo
  FROM v_alerta_stock_minimo
 ORDER BY sucursal, insumo;

-- Q7. Lotes vencidos o que vencen en los proximos 30 dias con stock remanente
SELECT s.nombre AS sucursal, i.nombre AS insumo, l.nro_lote, l.fecha_vencimiento,
       l.cantidad_actual,
       CASE WHEN l.fecha_vencimiento < CURDATE() THEN 'VENCIDO' ELSE 'PROXIMO A VENCER' END AS situacion
  FROM lote_insumo l
  JOIN insumo i   ON i.id_insumo   = l.id_insumo
  JOIN sucursal s ON s.id_sucursal = l.id_sucursal
 WHERE l.cantidad_actual > 0
   AND l.fecha_vencimiento <= DATE_ADD(CURDATE(), INTERVAL 30 DAY)
 ORDER BY l.fecha_vencimiento;

-- Q8. Carga de trabajo por veterinario (incluye a quienes no tienen turnos)
SELECT CONCAT(v.apellido, ', ', v.nombre) AS veterinario, s.nombre AS sucursal,
       COUNT(t.id_turno) AS turnos_asignados,
       COALESCE(SUM(t.estado = 'ATENDIDO'), 0) AS atendidos
  FROM veterinario v
  JOIN sucursal s ON s.id_sucursal = v.id_sucursal
  LEFT JOIN turno t ON t.id_veterinario = v.id_veterinario
 GROUP BY v.id_veterinario, v.apellido, v.nombre, s.nombre
 ORDER BY turnos_asignados DESC, veterinario;

-- Q9. Mascotas activas que nunca fueron atendidas (subconsulta NOT EXISTS)
SELECT m.nombre AS mascota, e.nombre AS especie,
       CONCAT(c.apellido, ', ', c.nombre) AS cliente, c.telefono
  FROM mascota m
  JOIN especie e ON e.id_especie = m.id_especie
  JOIN cliente c ON c.id_cliente = m.id_cliente
 WHERE m.activa = TRUE
   AND NOT EXISTS (SELECT 1 FROM turno t
                    WHERE t.id_mascota = m.id_mascota AND t.estado = 'ATENDIDO')
 ORDER BY cliente;
