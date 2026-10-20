-- =====================================================================
-- VetGest - Insercion de datos de prueba (INSERT)
-- Contrasena de todos los usuarios de ejemplo: VetGest2026! (hash bcrypt)
-- =====================================================================
USE vetgest_db;

INSERT INTO sucursal (nombre, direccion, telefono) VALUES
 ('Centro',       'Av. Colon 1250, Cordoba',          '0351-4221100'),
 ('Nueva Cordoba','Bv. Chacabuco 890, Cordoba',       '0351-4687720'),
 ('Cerro',        'Av. Rafael Nunez 4520, Cordoba',   '0351-4814455');

INSERT INTO especie (nombre) VALUES ('Canino'), ('Felino'), ('Exotico');

INSERT INTO cliente (dni, nombre, apellido, telefono, email) VALUES
 ('30111222','Laura','Fernandez','351-5551001','laura.fernandez@mail.com'),
 ('28999333','Martin','Sosa','351-5551002','msosa@mail.com'),
 ('35444555','Carla','Ibarra','351-5551003',NULL),
 ('40123456','Diego','Moreno','351-5551004','dmoreno@mail.com'),
 ('33777888','Sofia','Quiroga','351-5551005','sofiaq@mail.com'),
 ('29555666','Julian','Paz','351-5551006','jpaz@mail.com');

INSERT INTO mascota (id_cliente, id_especie, nombre, raza, sexo, fecha_nac) VALUES
 (1,1,'Toby','Caniche','M','2019-04-12'),
 (1,2,'Mishi','Europeo','H','2021-08-03'),
 (2,1,'Rocco','Labrador','M','2017-11-20'),
 (3,2,'Luna','Siames','H','2022-02-14'),
 (4,1,'Kira','Mestizo','H','2020-06-30'),
 (5,3,'Pancho','Conejo enano','M','2023-01-09'),
 (6,1,'Bruno','Bulldog frances','M','2021-12-01'),
 (6,1,'Nala','Golden retriever','H','2024-03-18');

INSERT INTO veterinario (id_sucursal, matricula, nombre, apellido, especialidad) VALUES
 (1,'MP-4521','Andrea','Gomez','Clinica general'),
 (1,'MP-3890','Ricardo','Paez','Cirugia'),
 (2,'MP-5012','Valeria','Luna','Clinica general'),
 (2,'MP-4777','Hernan','Ruiz','Dermatologia'),
 (3,'MP-5230','Paula','Diaz','Clinica general'),
 (3,'MP-4105','Tomas','Vera','Animales exoticos');

INSERT INTO usuario (id_sucursal, id_veterinario, username, password_hash, rol) VALUES
 (1,NULL,'recep.centro','$2b$10$Huy7HPxmqXDaPG5cAaM5surDI0Si2N.vRRX4D9uI6qDNiIM/j.u0O','RECEPCION'),
 (2,NULL,'recep.norte', '$2b$10$As64.bCI0FTym8HGzzx/T.0ujFN.MqMRBSlLsAjIQPKh6U32r62SK','RECEPCION'),
 (1,1,   'dra.gomez',   '$2b$10$oPMEpSwHfvYUG02.4H4yLO6qZIwZxCfrfkvHxwI237DGuKzhxZ8J2','VETERINARIO'),
 (1,2,   'dr.paez',     '$2b$10$c0JdMujdMLkIbXdYchhIxenG49aUQyCUZQxwLm3SXrj7e8IedHE2W','VETERINARIO'),
 (2,3,   'dra.luna',    '$2b$10$nyGBhs.207O3NsAh1OJs8etG2c7iAbWEgSeBFDpzJC5NkJsWDCWVa','VETERINARIO'),
 (1,NULL,'stock.red',   '$2b$10$vI2em4V8PsvuJLIdItFQ5OYCTzbM.uYBZIz7TwU8wfFHv0OC6Raq2','STOCK'),
 (1,NULL,'admin',       '$2b$10$r8fhyGYcG398ibLXHF1YK.Ch8sfW.zj5O2GPVR1DCwE1WQLGL8oya','ADMIN');

INSERT INTO turno (id_mascota, id_veterinario, id_sucursal, id_usuario, fecha_hora, duracion_min, motivo, estado) VALUES
 (1,1,1,1,'2026-09-21 09:00:00',30,'Vacuna antirrabica anual','ATENDIDO'),
 (3,2,1,1,'2026-09-21 10:00:00',60,'Extraccion de quiste','ATENDIDO'),
 (4,3,2,2,'2026-09-22 11:00:00',30,'Control de rutina','ATENDIDO'),
 (5,1,1,1,'2026-09-22 16:00:00',30,'Vacuna sextuple','ATENDIDO'),
 (2,3,2,2,'2026-09-23 09:30:00',30,'Vomitos','AUSENTE'),
 (7,5,3,1,'2026-09-24 10:00:00',30,'Dermatitis','CANCELADO'),
 (8,1,1,1,'2026-09-28 09:00:00',30,'Primera consulta cachorro','CONFIRMADO'),
 (6,6,3,1,'2026-09-28 11:00:00',30,'Control dental conejo','RESERVADO'),
 (1,1,1,1,'2026-09-28 09:30:00',30,'Control post vacuna','RESERVADO'),
 (3,2,1,1,'2026-09-29 10:00:00',30,'Retiro de puntos','RESERVADO');

INSERT INTO historia_clinica (id_turno, peso_kg, diagnostico, tratamiento, observaciones) VALUES
 (1, 6.40,'Paciente sano','Aplicacion de vacuna antirrabica','Proximo refuerzo en 12 meses'),
 (2,31.20,'Quiste sebaceo en flanco izquierdo','Exeresis quirurgica bajo anestesia; antibiotico 7 dias','Retiro de puntos en 8 dias'),
 (3, 3.90,'Paciente sano','Desparasitacion interna',NULL),
 (4,14.80,'Paciente sano','Aplicacion de vacuna sextuple',NULL);

INSERT INTO insumo (codigo, nombre, unidad, stock_minimo) VALUES
 ('VAC-ANR','Vacuna antirrabica','dosis',10),
 ('VAC-SEX','Vacuna sextuple canina','dosis',8),
 ('ANT-AMX','Amoxicilina 500 mg','comprimido',30),
 ('DES-INT','Antiparasitario interno','comprimido',20),
 ('DSC-JER','Jeringa descartable 5 ml','unidad',50),
 ('ANE-KET','Ketamina 50 ml','frasco',2);

-- Lotes con su cantidad vigente (luego de los movimientos registrados abajo)
INSERT INTO lote_insumo (id_insumo, id_sucursal, nro_lote, fecha_vencimiento, cantidad_actual) VALUES
 (1,1,'AR-2611','2027-05-31',18),
 (1,2,'AR-2611','2027-05-31', 6),
 (2,1,'SX-2604','2026-10-15', 7),
 (2,1,'SX-2702','2027-02-28', 0),
 (3,1,'AM-2590','2027-08-31',46),
 (4,2,'DI-2533','2026-09-10',25),
 (4,2,'DI-2612','2027-12-31',11),
 (5,1,'JE-2601','2029-01-31',120),
 (6,1,'KE-2605','2027-03-31', 1),
 (1,3,'AR-2611','2027-05-31',14),
 (2,3,'SX-2702','2027-02-28',12),
 (5,3,'JE-2601','2029-01-31',80),
 (5,2,'JE-2601','2029-01-31',45);

INSERT INTO mov_inventario (id_lote, id_historia, id_usuario, tipo_mov, cantidad, fecha) VALUES
 (1,NULL,6,'REPOSICION',19,'2026-09-01 08:30:00'),
 (2,NULL,6,'REPOSICION', 6,'2026-09-01 08:40:00'),
 (3,NULL,6,'REPOSICION', 8,'2026-09-01 08:45:00'),
 (5,NULL,6,'REPOSICION',60,'2026-09-01 09:00:00'),
 (7,NULL,6,'REPOSICION',12,'2026-09-02 09:00:00'),
 (8,NULL,6,'REPOSICION',125,'2026-09-02 09:10:00'),
 (9,NULL,6,'REPOSICION', 2,'2026-09-02 09:20:00'),
 (10,NULL,6,'REPOSICION',14,'2026-09-03 08:30:00'),
 (11,NULL,6,'REPOSICION',12,'2026-09-03 08:35:00'),
 (12,NULL,6,'REPOSICION',80,'2026-09-03 08:40:00'),
 (13,NULL,6,'REPOSICION',45,'2026-09-03 08:45:00'),
 (1,1,3,'CONSUMO', 1,'2026-09-21 09:20:00'),
 (8,1,3,'CONSUMO', 1,'2026-09-21 09:20:00'),
 (9,2,4,'CONSUMO', 1,'2026-09-21 10:40:00'),
 (5,2,4,'CONSUMO',14,'2026-09-21 10:45:00'),
 (8,2,4,'CONSUMO', 3,'2026-09-21 10:45:00'),
 (7,3,5,'CONSUMO', 1,'2026-09-22 11:20:00'),
 (3,4,3,'CONSUMO', 1,'2026-09-22 16:15:00'),
 (8,4,3,'CONSUMO', 1,'2026-09-22 16:15:00');
