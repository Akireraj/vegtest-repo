# VetGest — Proyecto integrador AP4 (PatasSanas)

Sistema de gestión de turnos, historias clínicas e inventario para **PatasSanas**, una red de tres clínicas veterinarias de Córdoba. Proyecto del *Seminario de práctica de informática* (Licenciatura en Informática) — autora: **Erika Rajoy**.

| Entrega | Contenido |
|---|---|
| AP1 | Modelo de negocio, requerimientos, casos de uso y prototipo inicial (fase de Inicio del PUD) |
| AP2 | Análisis, diseño, base de datos MySQL normalizada (scripts `sql/01` a `sql/06`), pruebas y comunicaciones |
| AP3 | Prototipo operacional en Java con POO, menú, estructuras de datos y algoritmos |
| **AP4** | **Versión integradora: patrón Fábrica abstracta + DAO justificado, grilla de disponibilidad con arreglos y `ArrayList`, reportes CSV en archivos y 45 pruebas** (este código) |

## Stack

- Java 17 o superior · Maven · JUnit 5
- MySQL 8.x (InnoDB, `utf8mb4`) mediante JDBC (`mysql-connector-j` 8.4)
- Arquitectura en capas: `ui` → `servicio` → `dao` (interfaces) → `dao.jdbc` / `dao.memoria` → MySQL

## Patrón de diseño

**Fábrica abstracta (Abstract Factory) combinada con DAO.** `FabricaDAO` es la fábrica abstracta; `FabricaDAOJdbc` y `FabricaDAOMemoria` son las fábricas concretas, cada una crea su familia completa de DAO (catálogo, turnos, historias e inventario). `MainApp` elige la familia al iniciar y los servicios trabajan solo con interfaces. Patrones de apoyo: Método plantilla (`Menu.mostrar()`, `MovimientoInventario.aplicarA()`) y Método de fábrica (`Mascota.crear()`).

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
    │   ├── ui/           Menu (abstracta) y submenús, Consola, Sesion, Formato
    │   ├── servicio/     TurnoService, HistoriaClinicaService, InventarioService,
    │   │                 SalaEsperaService, PacienteService, ReporteService,
    │   │                 GrillaDisponibilidad, Bitacora
    │   ├── dao/          interfaces + FabricaDAO (fábrica abstracta)
    │   │   ├── jdbc/     implementación MySQL (PreparedStatement, transacciones)
    │   │   └── memoria/  implementación sin base de datos (pruebas y demostración)
    │   ├── modelo/       Persona, Cliente, Veterinario, Mascota (Canino, Felino, Exotico),
    │   │                 Turno, EstadoTurno, MovimientoInventario (Consumo, Reposicion)...
    │   ├── archivo/      ExportadorCSV (guardar y recuperar reportes)
    │   ├── estructuras/  ListaEnlazada<T>, Cola<T>, Pila<T>
    │   ├── algoritmos/   Ordenamiento (merge sort, inserción), Busqueda (binaria, lineal)
    │   ├── excepcion/    jerarquía VetGestException (incluye ArchivoException)
    │   └── util/         ConexionBD, RelojSimulado
    └── test/java/        45 pruebas JUnit 5
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
mvn test                                   # 45 pruebas unitarias
mvn compile exec:java                      # menú interactivo
mvn package && java -jar target/vetgest.jar --demo
```

Argumentos de `MainApp`:

| Argumento | Efecto |
|---|---|
| `--memoria` | Usa los DAO en memoria con los datos de prueba (no requiere MySQL) |
| `--demo` | Ejecuta el escenario end-to-end automático y termina |
| `--hoy=AAAA-MM-DD` | Simula la fecha actual (útil para demostraciones) |

Si no encuentra `db.properties` o MySQL no responde, el programa avisa y continúa en modo memoria. Los reportes CSV se guardan en la carpeta `reportes/` del directorio de ejecución (excluida del repositorio).

Desde el IDE (IntelliJ IDEA, NetBeans o Eclipse): abrir la carpeta como proyecto Maven y ejecutar `ar.edu.vetgest.app.MainApp`.

### Sin Maven

```bash
javac --release 17 -encoding UTF-8 -d out $(find src/main/java -name "*.java")
java -cp out ar.edu.vetgest.app.MainApp --memoria
```

## Conceptos aplicados

| Concepto | Dónde |
|---|---|
| Patrón de diseño | Fábrica abstracta + DAO (`FabricaDAO`); Método plantilla; Método de fábrica |
| Clases abstractas e interfaces | `Persona`, `Mascota`, `MovimientoInventario`, `Menu`, `VetGestException`; interfaces DAO y `FabricaDAO` |
| Persistencia MySQL | `ConexionBD` + DAO JDBC: conexión, consultas, actualizaciones y transacciones |
| Excepciones | Jerarquía propia no comprobada, traducción de `SQLException` e `IOException`, `try-with-resources`, `commit`/`rollback`, multi-catch |
| Arreglos y `ArrayList` | `GrillaDisponibilidad` (`boolean[][]`, `int[]`, `Veterinario[]` ↔ `ArrayList`), `ExportadorCSV` (`String[]` por fila en `ArrayList<String[]>`), menús (`String[]`) |
| Archivos | `ExportadorCSV` y `ReporteService`: guardar y recuperar la agenda y las alertas en CSV |
| Estructuras de datos | Lista enlazada (tablas en memoria), cola FIFO (sala de espera), pila (deshacer y bitácora) |
| Algoritmos | Merge sort, inserción (FEFO), búsqueda binaria por DNI, búsqueda lineal por nombre |

## Requerimientos cubiertos

RF01 a RF09 implementados en servicios y menú (RF09 incluye la exportación de reportes a CSV); RF10 con menú según rol (la autenticación con bcrypt se incorpora con la interfaz gráfica).
