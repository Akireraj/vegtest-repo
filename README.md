# VetGest — Prototipo AP3 (PatasSanas)

Sistema de gestión de turnos, historias clínicas e inventario para **PatasSanas**, una red de tres clínicas veterinarias de Córdoba. Proyecto del *Seminario de práctica de informática* (Licenciatura en Informática) — autora: **Erika Rajoy**.

| Entrega | Contenido |
|---|---|
| AP1 | Modelo de negocio, requerimientos, casos de uso y prototipo inicial (fase de Inicio del PUD) |
| AP2 | Análisis, diseño, base de datos MySQL normalizada (scripts `sql/01` a `sql/06`), pruebas y comunicaciones |
| **AP3** | **Prototipo operacional en Java con POO, menú, estructuras de datos y algoritmos** (este código) |

## Stack

- Java 17 o superior · Maven · JUnit 5
- MySQL 8.x (InnoDB, `utf8mb4`) mediante JDBC (`mysql-connector-j` 8.4)
- Arquitectura en capas: `ui` → `servicio` → `dao` (interfaces) → `dao.jdbc` / `dao.memoria` → MySQL

## Estructura

```
vetgest/
├── pom.xml
├── db.properties.example      # copiar como db.properties
├── sql/                       # 01 a 07: creación, datos, consultas, ABM, integridad, usuario, mejoras AP3
├── docs/diagramas/            # fuentes PlantUML (.puml) e imágenes de los diagramas
└── src/
    ├── main/java/ar/edu/vetgest/
    │   ├── app/          MainApp, EscenarioDemo
    │   ├── ui/           Menu (abstracta) y submenús, Consola, Sesion
    │   ├── servicio/     TurnoService, HistoriaClinicaService, InventarioService,
    │   │                 SalaEsperaService, PacienteService, Bitacora
    │   ├── dao/          interfaces + FabricaDAO
    │   │   ├── jdbc/     implementación MySQL (PreparedStatement, transacciones)
    │   │   └── memoria/  implementación sin base de datos (pruebas y demostración)
    │   ├── modelo/       Persona, Cliente, Veterinario, Mascota (Canino, Felino, Exotico),
    │   │                 Turno, EstadoTurno, MovimientoInventario (Consumo, Reposicion)...
    │   ├── estructuras/  ListaEnlazada<T>, Cola<T>, Pila<T>
    │   ├── algoritmos/   Ordenamiento (merge sort, inserción), Busqueda (binaria, lineal)
    │   ├── excepcion/    jerarquía VetGestException
    │   └── util/         ConexionBD, RelojSimulado
    └── test/java/        35 pruebas JUnit 5
```

## Puesta en marcha

### 1. Base de datos (como `root`)

```bash
mysql -u root -p < sql/01_creacion_tablas.sql
mysql -u root -p < sql/02_insercion_datos.sql
mysql -u root -p < sql/06_usuario_aplicacion.sql
mysql -u root -p < sql/07_mejoras_ap3.sql
```

Los scripts `03`, `04` y `05` son de consulta, actualización y pruebas de integridad (AP2). El `05` debe ejecutarse con `mysql --force` porque todas sus sentencias fallan a propósito.

### 2. Conexión

Copiar `db.properties.example` como `db.properties` en la raíz del proyecto y completar la clave del usuario `vetgest_app`.

### 3. Compilar, probar y ejecutar

```bash
mvn test                                   # 35 pruebas unitarias
mvn compile exec:java                      # menú interactivo
mvn package && java -jar target/vetgest.jar --demo
```

Argumentos de `MainApp`:

| Argumento | Efecto |
|---|---|
| `--memoria` | Usa los DAO en memoria con los datos de prueba (no requiere MySQL) |
| `--demo` | Ejecuta el escenario end-to-end automático y termina |
| `--hoy=AAAA-MM-DD` | Simula la fecha actual (útil para demostraciones) |

Si no encuentra `db.properties` o MySQL no responde, el programa avisa y continúa en modo memoria.

Desde el IDE (IntelliJ IDEA, NetBeans o Eclipse): abrir la carpeta como proyecto Maven y ejecutar `ar.edu.vetgest.app.MainApp`.

### Sin Maven

```bash
javac --release 17 -encoding UTF-8 -d out $(find src/main/java -name "*.java")
java -cp out ar.edu.vetgest.app.MainApp --memoria
```

## Conceptos de POO aplicados

| Concepto | Dónde |
|---|---|
| Abstracción | `Persona`, `Mascota`, `MovimientoInventario`, `Menu` (clases abstractas); interfaces DAO y `FabricaDAO` |
| Encapsulamiento | Atributos privados validados; el turno solo cambia de estado con `cambiarEstado()`; copias defensivas |
| Herencia | `Cliente`/`Veterinario` → `Persona`; `Canino`/`Felino`/`Exotico` → `Mascota`; `Consumo`/`Reposicion` → `MovimientoInventario`; submenús → `Menu`; `RelojSimulado` → `java.time.Clock` |
| Polimorfismo | Duración de consulta por especie, `getDelta()` de cada movimiento, `toString()` de `Persona`, DAO JDBC o en memoria elegidos en ejecución |
| Excepciones | Jerarquía propia no comprobada, `try-with-resources`, `commit`/`rollback`, multi-catch, validación de entradas |
| Estructuras de datos | Lista enlazada (tablas en memoria), cola FIFO (sala de espera), pila (deshacer y bitácora) |
| Algoritmos | Merge sort, inserción (FEFO), búsqueda binaria por DNI, búsqueda lineal por nombre |

## Requerimientos cubiertos

RF01 a RF09 implementados en servicios y menú; RF10 con menú según rol (la autenticación con bcrypt se incorpora con la interfaz gráfica).
