package ar.edu.vetgest.dao.memoria;

import ar.edu.vetgest.estructuras.ListaEnlazada;
import ar.edu.vetgest.excepcion.EntidadNoEncontradaException;
import ar.edu.vetgest.modelo.Cliente;
import ar.edu.vetgest.modelo.EstadoTurno;
import ar.edu.vetgest.modelo.Especie;
import ar.edu.vetgest.modelo.HistoriaClinica;
import ar.edu.vetgest.modelo.Insumo;
import ar.edu.vetgest.modelo.LoteInsumo;
import ar.edu.vetgest.modelo.Mascota;
import ar.edu.vetgest.modelo.MovimientoInventario;
import ar.edu.vetgest.modelo.Rol;
import ar.edu.vetgest.modelo.Sucursal;
import ar.edu.vetgest.modelo.Turno;
import ar.edu.vetgest.modelo.Usuario;
import ar.edu.vetgest.modelo.Veterinario;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * "Base de datos" en memoria: cada tabla es una ListaEnlazada propia.
 * Permite ejecutar el prototipo y las pruebas unitarias sin MySQL. Los
 * datos de prueba replican el script sql/02_insercion_datos.sql.
 */
public class BaseDatosMemoria {

    final Clock reloj;
    final ListaEnlazada<Sucursal> sucursales = new ListaEnlazada<>();
    final ListaEnlazada<Usuario> usuarios = new ListaEnlazada<>();
    final ListaEnlazada<Veterinario> veterinarios = new ListaEnlazada<>();
    final ListaEnlazada<Cliente> clientes = new ListaEnlazada<>();
    final ListaEnlazada<Mascota> mascotas = new ListaEnlazada<>();
    final ListaEnlazada<Turno> turnos = new ListaEnlazada<>();
    final ListaEnlazada<HistoriaClinica> historias = new ListaEnlazada<>();
    final ListaEnlazada<Insumo> insumos = new ListaEnlazada<>();
    final ListaEnlazada<LoteInsumo> lotes = new ListaEnlazada<>();
    final ListaEnlazada<MovimientoInventario> movimientos = new ListaEnlazada<>();

    // secuencias que emulan AUTO_INCREMENT
    int secCliente;
    int secMascota;
    int secTurno;
    int secHistoria;
    int secLote;

    private BaseDatosMemoria(Clock reloj) {
        this.reloj = reloj;
    }

    public static BaseDatosMemoria vacia(Clock reloj) {
        return new BaseDatosMemoria(reloj);
    }

    public static BaseDatosMemoria conDatosDePrueba(Clock reloj) {
        BaseDatosMemoria db = new BaseDatosMemoria(reloj);
        db.cargarDatosDePrueba();
        return db;
    }

    LocalDate hoy() {
        return LocalDate.now(reloj);
    }

    // ---------------------------------------------------------------- busquedas por clave
    Sucursal sucursal(int id) {
        return sucursales.buscar(s -> s.getId() == id)
                .orElseThrow(() -> new EntidadNoEncontradaException("sucursal", id));
    }

    Veterinario veterinario(int id) {
        return veterinarios.buscar(v -> v.getId() == id)
                .orElseThrow(() -> new EntidadNoEncontradaException("veterinario", id));
    }

    Mascota mascota(int id) {
        return mascotas.buscar(m -> m.getId() == id)
                .orElseThrow(() -> new EntidadNoEncontradaException("mascota", id));
    }

    Cliente cliente(int id) {
        return clientes.buscar(c -> c.getId() == id)
                .orElseThrow(() -> new EntidadNoEncontradaException("cliente", id));
    }

    Insumo insumo(int id) {
        return insumos.buscar(i -> i.getId() == id)
                .orElseThrow(() -> new EntidadNoEncontradaException("insumo", id));
    }

    Turno turno(int id) {
        return turnos.buscar(t -> t.getIdTurno() == id)
                .orElseThrow(() -> new EntidadNoEncontradaException("turno", id));
    }

    // ---------------------------------------------------------------- datos de prueba
    private void cargarDatosDePrueba() {
        sucursales.agregar(new Sucursal(1, "Centro", "Av. Colon 1250, Cordoba", "0351-4221100"));
        sucursales.agregar(new Sucursal(2, "Nueva Cordoba", "Bv. Chacabuco 890, Cordoba", "0351-4687720"));
        sucursales.agregar(new Sucursal(3, "Cerro", "Av. Rafael Nunez 4520, Cordoba", "0351-4814455"));

        String[][] cli = {
                {"30111222", "Laura", "Fernandez", "351-5551001", "laura.fernandez@mail.com"},
                {"28999333", "Martin", "Sosa", "351-5551002", "msosa@mail.com"},
                {"35444555", "Carla", "Ibarra", "351-5551003", null},
                {"40123456", "Diego", "Moreno", "351-5551004", "dmoreno@mail.com"},
                {"33777888", "Sofia", "Quiroga", "351-5551005", "sofiaq@mail.com"},
                {"29555666", "Julian", "Paz", "351-5551006", "jpaz@mail.com"}};
        for (String[] c : cli) {
            clientes.agregar(new Cliente(++secCliente, c[0], c[1], c[2], c[3], c[4]));
        }

        agregarMascota(1, Especie.CANINO, "Toby", "Caniche", 'M', "2019-04-12");
        agregarMascota(1, Especie.FELINO, "Mishi", "Europeo", 'H', "2021-08-03");
        agregarMascota(2, Especie.CANINO, "Rocco", "Labrador", 'M', "2017-11-20");
        agregarMascota(3, Especie.FELINO, "Luna", "Siames", 'H', "2022-02-14");
        agregarMascota(4, Especie.CANINO, "Kira", "Mestizo", 'H', "2020-06-30");
        agregarMascota(5, Especie.EXOTICO, "Pancho", "Conejo enano", 'M', "2023-01-09");
        agregarMascota(6, Especie.CANINO, "Bruno", "Bulldog frances", 'M', "2021-12-01");
        agregarMascota(6, Especie.CANINO, "Nala", "Golden retriever", 'H', "2024-03-18");

        veterinarios.agregar(new Veterinario(1, 1, "MP-4521", "Andrea", "Gomez", "Clinica general"));
        veterinarios.agregar(new Veterinario(2, 1, "MP-3890", "Ricardo", "Paez", "Cirugia"));
        veterinarios.agregar(new Veterinario(3, 2, "MP-5012", "Valeria", "Luna", "Clinica general"));
        veterinarios.agregar(new Veterinario(4, 2, "MP-4777", "Hernan", "Ruiz", "Dermatologia"));
        veterinarios.agregar(new Veterinario(5, 3, "MP-5230", "Paula", "Diaz", "Clinica general"));
        veterinarios.agregar(new Veterinario(6, 3, "MP-4105", "Tomas", "Vera", "Animales exoticos"));

        usuarios.agregar(new Usuario(1, 1, null, "recep.centro", Rol.RECEPCION));
        usuarios.agregar(new Usuario(2, 2, null, "recep.norte", Rol.RECEPCION));
        usuarios.agregar(new Usuario(3, 1, 1, "dra.gomez", Rol.VETERINARIO));
        usuarios.agregar(new Usuario(4, 1, 2, "dr.paez", Rol.VETERINARIO));
        usuarios.agregar(new Usuario(5, 2, 3, "dra.luna", Rol.VETERINARIO));
        usuarios.agregar(new Usuario(6, 1, null, "stock.red", Rol.STOCK));
        usuarios.agregar(new Usuario(7, 1, null, "admin", Rol.ADMIN));

        agregarTurno(1, 1, 1, 1, "2026-09-21T09:00", 30, "Vacuna antirrabica anual", EstadoTurno.ATENDIDO);
        agregarTurno(3, 2, 1, 1, "2026-09-21T10:00", 60, "Extraccion de quiste", EstadoTurno.ATENDIDO);
        agregarTurno(4, 3, 2, 2, "2026-09-22T11:00", 30, "Control de rutina", EstadoTurno.ATENDIDO);
        agregarTurno(5, 1, 1, 1, "2026-09-22T16:00", 30, "Vacuna sextuple", EstadoTurno.ATENDIDO);
        agregarTurno(2, 3, 2, 2, "2026-09-23T09:30", 30, "Vomitos", EstadoTurno.AUSENTE);
        agregarTurno(7, 5, 3, 1, "2026-09-24T10:00", 30, "Dermatitis", EstadoTurno.CANCELADO);
        agregarTurno(8, 1, 1, 1, "2026-09-28T09:00", 30, "Primera consulta cachorro", EstadoTurno.CONFIRMADO);
        agregarTurno(6, 6, 3, 1, "2026-09-28T11:00", 30, "Control dental conejo", EstadoTurno.RESERVADO);
        agregarTurno(1, 1, 1, 1, "2026-09-28T09:30", 30, "Control post vacuna", EstadoTurno.RESERVADO);
        agregarTurno(3, 2, 1, 1, "2026-09-29T10:00", 30, "Retiro de puntos", EstadoTurno.RESERVADO);

        agregarHistoria(1, "6.40", "Paciente sano", "Aplicacion de vacuna antirrabica", "Proximo refuerzo en 12 meses");
        agregarHistoria(2, "31.20", "Quiste sebaceo en flanco izquierdo",
                "Exeresis quirurgica bajo anestesia; antibiotico 7 dias", "Retiro de puntos en 8 dias");
        agregarHistoria(3, "3.90", "Paciente sano", "Desparasitacion interna", null);
        agregarHistoria(4, "14.80", "Paciente sano", "Aplicacion de vacuna sextuple", null);

        insumos.agregar(new Insumo(1, "VAC-ANR", "Vacuna antirrabica", "dosis", 10));
        insumos.agregar(new Insumo(2, "VAC-SEX", "Vacuna sextuple canina", "dosis", 8));
        insumos.agregar(new Insumo(3, "ANT-AMX", "Amoxicilina 500 mg", "comprimido", 30));
        insumos.agregar(new Insumo(4, "DES-INT", "Antiparasitario interno", "comprimido", 20));
        insumos.agregar(new Insumo(5, "DSC-JER", "Jeringa descartable 5 ml", "unidad", 50));
        insumos.agregar(new Insumo(6, "ANE-KET", "Ketamina 50 ml", "frasco", 2));

        agregarLote(1, 1, "AR-2611", "2027-05-31", 18);
        agregarLote(1, 2, "AR-2611", "2027-05-31", 6);
        agregarLote(2, 1, "SX-2604", "2026-10-15", 7);
        agregarLote(2, 1, "SX-2702", "2027-02-28", 0);
        agregarLote(3, 1, "AM-2590", "2027-08-31", 46);
        agregarLote(4, 2, "DI-2533", "2026-09-10", 25);
        agregarLote(4, 2, "DI-2612", "2027-12-31", 11);
        agregarLote(5, 1, "JE-2601", "2029-01-31", 120);
        agregarLote(6, 1, "KE-2605", "2027-03-31", 1);
        agregarLote(1, 3, "AR-2611", "2027-05-31", 14);
        agregarLote(2, 3, "SX-2702", "2027-02-28", 12);
        agregarLote(5, 3, "JE-2601", "2029-01-31", 80);
        agregarLote(5, 2, "JE-2601", "2029-01-31", 45);
    }

    private void agregarMascota(int idCliente, Especie especie, String nombre, String raza, char sexo, String nac) {
        Mascota m = Mascota.crear(especie, ++secMascota, idCliente, nombre, raza, sexo, LocalDate.parse(nac));
        mascotas.agregar(m);
        cliente(idCliente).agregarMascota(m);
    }

    private void agregarTurno(int idMascota, int idVet, int idSuc, int idUsr, String fecha, int min, String motivo,
                              EstadoTurno estado) {
        turnos.agregar(new Turno(++secTurno, idMascota, idVet, idSuc, idUsr, LocalDateTime.parse(fecha), min,
                motivo, estado));
    }

    private void agregarHistoria(int idTurno, String peso, String diag, String trat, String obs) {
        HistoriaClinica h = new HistoriaClinica(idTurno, new BigDecimal(peso), diag, trat, obs);
        h.asignarId(++secHistoria);
        historias.agregar(h);
    }

    private void agregarLote(int idInsumo, int idSuc, String nro, String vence, int cantidad) {
        LoteInsumo l = new LoteInsumo(++secLote, idInsumo, idSuc, nro, LocalDate.parse(vence), cantidad);
        l.setNombreInsumo(insumo(idInsumo).getNombre());
        lotes.agregar(l);
    }
}
