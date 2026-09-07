# VetGest — Prototipo (PatasSanas)

Prototipo operacional desarrollado para la Actividad Práctica 1 de *Análisis
y Diseño de Software* (Proceso Unificado de Desarrollo — fase de Inicio),
correspondiente al proyecto **VetGest** para la red de clínicas veterinarias
**PatasSanas**.

Siguiendo el alcance definido en el informe (sección 3.3), este prototipo
implementa **únicamente los módulos de Turnos e Inventario**: son los que
concentran las reglas de negocio más críticas relevadas (no superposición de
turnos y descuento automático de stock con alerta de mínimo). El módulo de
Historia Clínica está modelado en la base de datos (es indispensable para
vincular el consumo de insumos a una atención concreta) pero no cuenta con
una capa de servicio propia en esta iteración; se profundizará en las fases
de Elaboración y Construcción, junto con la interfaz gráfica.

## Stack tecnológico

- **Lenguaje:** Java 17
- **Persistencia:** MySQL 8.x (driver JDBC `mysql-connector-j`), base
  `vetgest_db`
- **Gestor de dependencias:** Maven
- **Arquitectura:** por capas — `modelo` / `dao` (JDBC puro,
  `PreparedStatement`, transacciones explícitas) / `servicio` (reglas de
  negocio) / `MainApp` (escenario de prueba de consola)
- **Patrón:** DAO (Data Access Object), tal como se justifica en la sección
  6.3 del informe

## Estructura del proyecto

```
vetgest-prototipo/
├── pom.xml
├── README.md
├── .gitignore
├── sql/
│   └── vetgest_db.sql        # esquema (7 tablas) + datos de ejemplo
└── src/main/
    ├── resources/
    │   ├── config.properties         # credenciales de conexión (editar)
    │   └── config.properties.example
    └── java/com/vetgest/
        ├── MainApp.java               # escenario de prueba end-to-end
        ├── config/
        │   └── ConexionBD.java
        ├── modelo/
        │   ├── Mascota.java
        │   ├── Turno.java
        │   ├── Insumo.java
        │   ├── HistoriaClinica.java   # clase de apoyo (ver nota abajo)
        │   └── TipoMovimiento.java    # enum CONSUMO / REPOSICION
        ├── dao/
        │   ├── TurnoDAO.java          # turno + historia_clinica
        │   └── InsumoDAO.java        # insumo + mov_inventario
        └── servicio/
            ├── TurnoService.java      # RF01–RF04
            └── InventarioService.java # RF06–RF08
```

> **Nota de diseño — `HistoriaClinica`:** la sección 8.1 del informe lista
> como clases de modelo solo `Mascota`, `Turno` e `Insumo`. Sin embargo, el
> modelo entidad-relación (Figura 5) vincula `mov_inventario` a
> `historia_clinica` (no a `turno`), y el caso de uso "Registrar historia
> clínica" exige generar ese registro al atender un turno. Se agregó
> entonces una clase `HistoriaClinica` mínima, sin DAO ni servicio propios
> (la crea internamente `TurnoDAO.registrarAtencion(...)`), para que el
> flujo `TurnoService.atenderTurno(...) → InventarioService.registrarConsumo(...)`
> sea consistente con el esquema de base de datos.

## Requerimientos cubiertos por el prototipo

| Req. | Descripción | Dónde |
|---|---|---|
| RF01 | Registrar turno (mascota, veterinario, fecha y hora) | `TurnoService.reservarTurno` |
| RF02 | Rechazar turno superpuesto para el mismo veterinario/horario | `TurnoDAO.existeSuperposicion` |
| RF03 | Confirmar, reprogramar o cancelar un turno | `TurnoService.confirmarTurno / reprogramarTurno / cancelarTurno` |
| RF04 | Registrar historia clínica de una atención | `TurnoService.atenderTurno` |
| RF06 | Descontar stock al registrar consumo de insumos | `InventarioService.registrarConsumo` |
| RF07 | Registrar reposición de stock | `InventarioService.registrarReposicion` |
| RF08 | Alerta cuando el stock cae al mínimo o por debajo | `Insumo.isAlertaStockMinimo`, `InventarioService.consultarAlertasStockMinimo` |

RF05 (consultar historial clínico multi-sucursal), RF09 (reportes
gerenciales) y RF10 (gestión de usuarios y roles) quedan fuera del alcance
de este prototipo, tal como se explicita en la sección 3.3 del informe
("fuera del alcance de esta entrega").

## Puesta en marcha

### 1. Crear la base de datos

```bash
mysql -u root -p < sql/vetgest_db.sql
```

Esto crea `vetgest_db` con sus 7 tablas y datos de ejemplo: 2 clientes, 2
mascotas, 2 veterinarios y 4 insumos (uno de ellos, *Suero fisiológico
500ml*, ya arranca por debajo de su stock mínimo a propósito, para poder
observar una alerta preexistente).

### 2. Configurar la conexión

Editar `src/main/resources/config.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/vetgest_db?useSSL=false&serverTimezone=UTC
db.usuario=root
db.contrasenia=tu_contrasenia
```

### 3. Compilar y ejecutar

```bash
mvn clean package
java -jar target/vetgest-prototipo.jar
```

El programa ejecuta automáticamente el escenario de prueba end-to-end
descripto en la sección 8.1/8.2 del informe:

1. Reserva un turno (Firulais con la Dra. Rodríguez).
2. Intenta reservar otro turno para el mismo veterinario y horario →
   **rechazado** (RF02).
3. Confirma y atiende el turno original (se genera la historia clínica).
4. Registra el consumo de 2 dosis de "Vacuna antirrábica" asociado a esa
   atención → dispara alerta de stock mínimo (RF08).
5. Consulta y muestra todos los insumos en alerta de stock mínimo.

## Relación con el informe

Este código corresponde a la sección 8 ("Prototipo Java + MySQL") del
informe de la Actividad Práctica 1. El enlace a este repositorio debe
reemplazar el placeholder `https://github.com/[usuario]/vetgest-prototipo`
citado en la sección 8.2 del informe.

## Referencias

Kendall, K., & Kendall, J. (2011). *Análisis y diseño de sistemas* (8.a
ed.). Pearson Education.
