-- =====================================================================
-- VetGest - Script de creacion de la base de datos (MySQL 8.x)
-- AP2 - Seminario de practica de informatica - RAJOY, Erika
-- Modelo normalizado en 3FN. Motor InnoDB, charset utf8mb4.
-- =====================================================================
DROP DATABASE IF EXISTS vetgest_db;
CREATE DATABASE vetgest_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE vetgest_db;

-- ---------------------------------------------------------------------
-- Tablas maestras
-- ---------------------------------------------------------------------
CREATE TABLE sucursal (
  id_sucursal   INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre        VARCHAR(60)  NOT NULL,
  direccion     VARCHAR(120) NOT NULL,
  telefono      VARCHAR(20)  NOT NULL,
  CONSTRAINT uq_sucursal_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE especie (
  id_especie    SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre        VARCHAR(40) NOT NULL,
  CONSTRAINT uq_especie_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE cliente (
  id_cliente    INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  dni           VARCHAR(10)  NOT NULL,
  nombre        VARCHAR(60)  NOT NULL,
  apellido      VARCHAR(60)  NOT NULL,
  telefono      VARCHAR(20)  NOT NULL,
  email         VARCHAR(100) NULL,
  fecha_alta    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_cliente_dni UNIQUE (dni)
) ENGINE=InnoDB;

CREATE TABLE mascota (
  id_mascota    INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_cliente    INT UNSIGNED      NOT NULL,
  id_especie    SMALLINT UNSIGNED NOT NULL,
  nombre        VARCHAR(40)  NOT NULL,
  raza          VARCHAR(40)  NULL,
  sexo          CHAR(1)      NOT NULL,
  fecha_nac     DATE         NULL,
  activa        BOOLEAN      NOT NULL DEFAULT TRUE,
  CONSTRAINT ck_mascota_sexo CHECK (sexo IN ('M','H')),
  CONSTRAINT fk_mascota_cliente FOREIGN KEY (id_cliente)
      REFERENCES cliente(id_cliente) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_mascota_especie FOREIGN KEY (id_especie)
      REFERENCES especie(id_especie) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE veterinario (
  id_veterinario INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_sucursal    INT UNSIGNED NOT NULL,
  matricula      VARCHAR(15)  NOT NULL,
  nombre         VARCHAR(60)  NOT NULL,
  apellido       VARCHAR(60)  NOT NULL,
  especialidad   VARCHAR(60)  NOT NULL DEFAULT 'Clinica general',
  CONSTRAINT uq_veterinario_matricula UNIQUE (matricula),
  CONSTRAINT fk_veterinario_sucursal FOREIGN KEY (id_sucursal)
      REFERENCES sucursal(id_sucursal) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE usuario (
  id_usuario     INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_sucursal    INT UNSIGNED NOT NULL,
  id_veterinario INT UNSIGNED NULL,
  username       VARCHAR(30)  NOT NULL,
  password_hash  CHAR(60)     NOT NULL,           -- hash bcrypt (RNF01)
  rol            ENUM('RECEPCION','VETERINARIO','STOCK','ADMIN') NOT NULL,
  activo         BOOLEAN      NOT NULL DEFAULT TRUE,
  CONSTRAINT uq_usuario_username UNIQUE (username),
  CONSTRAINT uq_usuario_veterinario UNIQUE (id_veterinario),
  CONSTRAINT fk_usuario_sucursal FOREIGN KEY (id_sucursal)
      REFERENCES sucursal(id_sucursal) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_usuario_veterinario FOREIGN KEY (id_veterinario)
      REFERENCES veterinario(id_veterinario) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Modulo Turnos e Historia Clinica
-- ---------------------------------------------------------------------
CREATE TABLE turno (
  id_turno       INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_mascota     INT UNSIGNED NOT NULL,
  id_veterinario INT UNSIGNED NOT NULL,
  id_sucursal    INT UNSIGNED NOT NULL,
  id_usuario     INT UNSIGNED NOT NULL,          -- usuario que registro el turno
  fecha_hora     DATETIME     NOT NULL,
  duracion_min   SMALLINT UNSIGNED NOT NULL DEFAULT 30,
  motivo         VARCHAR(120) NOT NULL,
  estado         ENUM('RESERVADO','CONFIRMADO','ATENDIDO','CANCELADO','AUSENTE')
                 NOT NULL DEFAULT 'RESERVADO',
  fecha_registro DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_turno_duracion CHECK (duracion_min BETWEEN 10 AND 240),
  CONSTRAINT fk_turno_mascota FOREIGN KEY (id_mascota)
      REFERENCES mascota(id_mascota) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_turno_veterinario FOREIGN KEY (id_veterinario)
      REFERENCES veterinario(id_veterinario) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_turno_sucursal FOREIGN KEY (id_sucursal)
      REFERENCES sucursal(id_sucursal) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_turno_usuario FOREIGN KEY (id_usuario)
      REFERENCES usuario(id_usuario) ON UPDATE CASCADE ON DELETE RESTRICT,
  INDEX idx_turno_vet_fecha (id_veterinario, fecha_hora),
  INDEX idx_turno_sucursal_fecha (id_sucursal, fecha_hora)
) ENGINE=InnoDB;

CREATE TABLE historia_clinica (
  id_historia    INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_turno       INT UNSIGNED NOT NULL,
  peso_kg        DECIMAL(5,2) NOT NULL,
  diagnostico    VARCHAR(500) NOT NULL,
  tratamiento    VARCHAR(500) NOT NULL,
  observaciones  VARCHAR(500) NULL,
  fecha_registro DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_historia_turno UNIQUE (id_turno),     -- relacion 1:1 con turno
  CONSTRAINT ck_historia_peso CHECK (peso_kg > 0),
  CONSTRAINT fk_historia_turno FOREIGN KEY (id_turno)
      REFERENCES turno(id_turno) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Modulo Inventario
-- ---------------------------------------------------------------------
CREATE TABLE insumo (
  id_insumo      INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  codigo         VARCHAR(15)  NOT NULL,
  nombre         VARCHAR(80)  NOT NULL,
  unidad         VARCHAR(15)  NOT NULL,             -- dosis, ml, unidad, caja
  stock_minimo   INT UNSIGNED NOT NULL DEFAULT 0,   -- umbral por sucursal
  CONSTRAINT uq_insumo_codigo UNIQUE (codigo)
) ENGINE=InnoDB;

CREATE TABLE lote_insumo (
  id_lote          INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_insumo        INT UNSIGNED NOT NULL,
  id_sucursal      INT UNSIGNED NOT NULL,
  nro_lote         VARCHAR(20)  NOT NULL,
  fecha_vencimiento DATE        NOT NULL,
  cantidad_actual  INT          NOT NULL DEFAULT 0,
  CONSTRAINT ck_lote_cantidad CHECK (cantidad_actual >= 0),
  CONSTRAINT uq_lote UNIQUE (id_insumo, id_sucursal, nro_lote),
  CONSTRAINT fk_lote_insumo FOREIGN KEY (id_insumo)
      REFERENCES insumo(id_insumo) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_lote_sucursal FOREIGN KEY (id_sucursal)
      REFERENCES sucursal(id_sucursal) ON UPDATE CASCADE ON DELETE RESTRICT,
  INDEX idx_lote_vencimiento (fecha_vencimiento)
) ENGINE=InnoDB;

CREATE TABLE mov_inventario (
  id_mov         INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_lote        INT UNSIGNED NOT NULL,
  id_historia    INT UNSIGNED NULL,                -- obligatorio si tipo = CONSUMO
  id_usuario     INT UNSIGNED NOT NULL,
  tipo_mov       ENUM('CONSUMO','REPOSICION','AJUSTE') NOT NULL,
  cantidad       INT          NOT NULL,
  fecha          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_mov_cantidad CHECK (cantidad > 0),
  CONSTRAINT ck_mov_consumo CHECK (tipo_mov <> 'CONSUMO' OR id_historia IS NOT NULL),
  CONSTRAINT fk_mov_lote FOREIGN KEY (id_lote)
      REFERENCES lote_insumo(id_lote) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_mov_historia FOREIGN KEY (id_historia)
      REFERENCES historia_clinica(id_historia) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT fk_mov_usuario FOREIGN KEY (id_usuario)
      REFERENCES usuario(id_usuario) ON UPDATE CASCADE ON DELETE RESTRICT,
  INDEX idx_mov_fecha (fecha)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Trigger de integridad: segunda linea de defensa contra superposicion
-- (la validacion principal se realiza en TurnoService - RF02)
-- ---------------------------------------------------------------------
DELIMITER //
CREATE TRIGGER trg_turno_no_superposicion
BEFORE INSERT ON turno
FOR EACH ROW
BEGIN
  IF NEW.estado IN ('RESERVADO','CONFIRMADO') AND EXISTS (
       SELECT 1 FROM turno t
        WHERE t.id_veterinario = NEW.id_veterinario
          AND t.estado IN ('RESERVADO','CONFIRMADO')
          AND NEW.fecha_hora < DATE_ADD(t.fecha_hora, INTERVAL t.duracion_min MINUTE)
          AND t.fecha_hora  < DATE_ADD(NEW.fecha_hora, INTERVAL NEW.duracion_min MINUTE))
  THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Superposicion de turno para el veterinario (RF02)';
  END IF;
END//
DELIMITER ;

-- ---------------------------------------------------------------------
-- Vistas de apoyo (stock consolidado y alertas - RF08)
-- ---------------------------------------------------------------------
CREATE VIEW v_stock_sucursal AS
SELECT s.id_sucursal, s.nombre AS sucursal, i.id_insumo, i.codigo, i.nombre AS insumo,
       i.unidad, i.stock_minimo,
       SUM(CASE WHEN l.fecha_vencimiento >= CURDATE() THEN l.cantidad_actual ELSE 0 END)
           AS stock_vigente
  FROM lote_insumo l
  JOIN insumo   i ON i.id_insumo   = l.id_insumo
  JOIN sucursal s ON s.id_sucursal = l.id_sucursal
 GROUP BY s.id_sucursal, s.nombre, i.id_insumo, i.codigo, i.nombre, i.unidad, i.stock_minimo;

CREATE VIEW v_alerta_stock_minimo AS
SELECT * FROM v_stock_sucursal WHERE stock_vigente <= stock_minimo;
