-- =====================================================================
-- VetGest - Mejoras incorporadas en la AP3
-- Ejecutar como root despues de los scripts 01, 02 y 06.
-- =====================================================================
USE vetgest_db;

-- ---------------------------------------------------------------------
-- 1) El trigger de la AP2 solo controlaba INSERT. Una reprogramacion
--    (UPDATE de fecha_hora) o una reactivacion podia generar
--    superposiciones si se omitia la capa de servicios. Este trigger
--    aplica la misma regla RF02 a las actualizaciones.
-- ---------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_turno_no_superposicion_upd;
DELIMITER //
CREATE TRIGGER trg_turno_no_superposicion_upd
BEFORE UPDATE ON turno
FOR EACH ROW
BEGIN
  IF NEW.estado IN ('RESERVADO','CONFIRMADO')
     AND (NEW.fecha_hora <> OLD.fecha_hora
          OR NEW.duracion_min <> OLD.duracion_min
          OR NEW.id_veterinario <> OLD.id_veterinario)
     AND EXISTS (
       SELECT 1 FROM turno t
        WHERE t.id_turno <> NEW.id_turno
          AND t.id_veterinario = NEW.id_veterinario
          AND t.estado IN ('RESERVADO','CONFIRMADO')
          AND NEW.fecha_hora < DATE_ADD(t.fecha_hora, INTERVAL t.duracion_min MINUTE)
          AND t.fecha_hora  < DATE_ADD(NEW.fecha_hora, INTERVAL NEW.duracion_min MINUTE))
  THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Superposicion de turno para el veterinario al reprogramar (RF02)';
  END IF;
END//
DELIMITER ;

-- ---------------------------------------------------------------------
-- 2) Usuario de aplicacion para los clientes de las tres sedes
--    (subredes 10.10.x.0/24 de la seccion 7 de la AP2), con TLS
--    obligatorio. El usuario @localhost del script 06 queda para pruebas.
-- ---------------------------------------------------------------------
CREATE USER IF NOT EXISTS 'vetgest_app'@'10.10.%' IDENTIFIED BY 'cambiar_esta_clave' REQUIRE SSL;
GRANT SELECT, INSERT, UPDATE, DELETE ON vetgest_db.* TO 'vetgest_app'@'10.10.%';
FLUSH PRIVILEGES;
