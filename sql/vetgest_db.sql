-- =====================================================================
-- VetGest - PatasSanas (red de clinicas veterinarias)
-- Esquema de base de datos: vetgest_db (MySQL 8.x)
--
-- Modelo entidad-relacion segun Figura 5 del informe AP1:
-- cliente, mascota, veterinario, turno, historia_clinica, insumo,
-- mov_inventario.
--
-- El prototipo Java (MainApp) implementa unicamente los modulos de
-- Turnos e Inventario (ver seccion 8.1 del informe); las tablas cliente,
-- mascota y veterinario se precargan con datos de ejemplo para que el
-- prototipo pueda referenciarlas por id.
-- =====================================================================

DROP DATABASE IF EXISTS vetgest_db;
CREATE DATABASE vetgest_db CHARACTER SET utf8mb4 COLLATE utf8mb4_spanish_ci;
USE vetgest_db;

-- ---------------------------------------------------------------------
-- cliente
-- ---------------------------------------------------------------------
CREATE TABLE cliente (
    id_cliente   INT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(120) NOT NULL,
    telefono     VARCHAR(30),
    email        VARCHAR(120)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- mascota
-- ---------------------------------------------------------------------
CREATE TABLE mascota (
    id_mascota   INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente   INT NOT NULL,
    nombre       VARCHAR(60) NOT NULL,
    especie      VARCHAR(40) NOT NULL,
    fecha_nac    DATE,
    CONSTRAINT fk_mascota_cliente FOREIGN KEY (id_cliente)
        REFERENCES cliente(id_cliente)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- veterinario
-- ---------------------------------------------------------------------
CREATE TABLE veterinario (
    id_veterinario   INT AUTO_INCREMENT PRIMARY KEY,
    nombre           VARCHAR(120) NOT NULL,
    matricula        VARCHAR(30) NOT NULL UNIQUE,
    especialidad     VARCHAR(80)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- turno   (RF01, RF02, RF03)
-- ---------------------------------------------------------------------
CREATE TABLE turno (
    id_turno        INT AUTO_INCREMENT PRIMARY KEY,
    id_mascota      INT NOT NULL,
    id_veterinario  INT NOT NULL,
    fecha_hora      DATETIME NOT NULL,
    estado          ENUM('RESERVADO','CONFIRMADO','ATENDIDO','CANCELADO') NOT NULL DEFAULT 'RESERVADO',
    CONSTRAINT fk_turno_mascota FOREIGN KEY (id_mascota)
        REFERENCES mascota(id_mascota)
        ON DELETE CASCADE,
    CONSTRAINT fk_turno_veterinario FOREIGN KEY (id_veterinario)
        REFERENCES veterinario(id_veterinario)
        ON DELETE RESTRICT,
    -- Refuerza a nivel de motor la regla RF02 (no dos turnos activos del
    -- mismo veterinario en el mismo horario). La validacion "amigable"
    -- (con mensaje de negocio) se hace ademas desde TurnoService antes
    -- de insertar.
    INDEX idx_turno_veterinario_fecha (id_veterinario, fecha_hora)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- historia_clinica   (1:1 con turno) (RF04, RF05)
-- ---------------------------------------------------------------------
CREATE TABLE historia_clinica (
    id_historia   INT AUTO_INCREMENT PRIMARY KEY,
    id_turno      INT NOT NULL UNIQUE,
    diagnostico   TEXT,
    tratamiento   TEXT,
    fecha         DATE NOT NULL,
    CONSTRAINT fk_historia_turno FOREIGN KEY (id_turno)
        REFERENCES turno(id_turno)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- insumo   (RF06, RF07, RF08)
-- ---------------------------------------------------------------------
CREATE TABLE insumo (
    id_insumo      INT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(120) NOT NULL,
    stock_actual   INT NOT NULL DEFAULT 0,
    stock_minimo   INT NOT NULL DEFAULT 0,
    vencimiento    DATE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- mov_inventario   (RF06, RF07)
-- ---------------------------------------------------------------------
CREATE TABLE mov_inventario (
    id_mov       INT AUTO_INCREMENT PRIMARY KEY,
    id_insumo    INT NOT NULL,
    id_historia  INT,                 -- NULL cuando el movimiento es una reposicion
    cantidad     INT NOT NULL,
    tipo_mov     ENUM('CONSUMO','REPOSICION') NOT NULL,
    fecha        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mov_insumo FOREIGN KEY (id_insumo)
        REFERENCES insumo(id_insumo)
        ON DELETE CASCADE,
    CONSTRAINT fk_mov_historia FOREIGN KEY (id_historia)
        REFERENCES historia_clinica(id_historia)
        ON DELETE SET NULL
) ENGINE=InnoDB;

-- =====================================================================
-- Datos de ejemplo (para que MainApp pueda ejecutar el escenario de
-- prueba end-to-end referenciando ids existentes de cliente, mascota
-- y veterinario).
-- =====================================================================

INSERT INTO cliente (nombre, telefono, email) VALUES
('Juan Perez', '351-444-5555', 'juan.perez@mail.com'),
('Ana Gomez',  '351-666-7777', 'ana.gomez@mail.com');

INSERT INTO mascota (id_cliente, nombre, especie, fecha_nac) VALUES
(1, 'Firulais', 'Perro', '2021-03-15'),
(2, 'Michi',    'Gato',  '2022-07-01');

INSERT INTO veterinario (nombre, matricula, especialidad) VALUES
('Dra. Maria Rodriguez', 'MP-12345', 'Clinica general'),
('Dr. Carlos Fernandez', 'MP-67890', 'Cirugia');

-- Insumos de ejemplo. "Sueros fisiologico 500ml" ya arranca por debajo
-- del minimo (para ver una alerta preexistente); "Vacuna antirrabica"
-- queda justo en el limite para disparar una alerta nueva al
-- registrarse el consumo durante el escenario de prueba de MainApp.
INSERT INTO insumo (nombre, stock_actual, stock_minimo, vencimiento) VALUES
('Vacuna antirrabica',         6,  5, '2027-02-28'),
('Jeringa descartable 5ml',   200, 50, NULL),
('Amoxicilina inyectable',     30, 10, '2026-12-31'),
('Suero fisiologico 500ml',    15, 20, '2027-06-30');
