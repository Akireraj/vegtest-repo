package ar.edu.vetgest.app;

import ar.edu.vetgest.dao.FabricaDAO;
import ar.edu.vetgest.dao.jdbc.FabricaDAOJdbc;
import ar.edu.vetgest.dao.memoria.BaseDatosMemoria;
import ar.edu.vetgest.dao.memoria.FabricaDAOMemoria;
import ar.edu.vetgest.excepcion.DatosException;
import ar.edu.vetgest.excepcion.EntidadNoEncontradaException;
import ar.edu.vetgest.excepcion.VetGestException;
import ar.edu.vetgest.modelo.Sucursal;
import ar.edu.vetgest.modelo.Usuario;
import ar.edu.vetgest.ui.Consola;
import ar.edu.vetgest.ui.EntradaFinalizadaException;
import ar.edu.vetgest.ui.MenuPrincipal;
import ar.edu.vetgest.ui.Sesion;
import ar.edu.vetgest.util.ConexionBD;
import ar.edu.vetgest.util.RelojSimulado;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Punto de entrada de VetGest.
 * Argumentos: --memoria (sin MySQL), --demo (escenario automatico),
 * --hoy=AAAA-MM-DD (simula la fecha actual), --ayuda.
 */
public final class MainApp {

    private MainApp() {
    }

    public static void main(String[] args) {
        boolean memoria = false;
        boolean demo = false;
        LocalDate hoySimulado = null;
        for (String arg : args) {
            if (arg.equals("--memoria")) {
                memoria = true;
            } else if (arg.equals("--demo")) {
                demo = true;
            } else if (arg.startsWith("--hoy=")) {
                try {
                    hoySimulado = LocalDate.parse(arg.substring("--hoy=".length()));
                } catch (DateTimeParseException e) {
                    System.err.println("Fecha invalida en " + arg + "; se usa la fecha del sistema");
                }
            } else if (arg.equals("--ayuda")) {
                System.out.println("Uso: java -jar vetgest.jar [--memoria] [--demo] [--hoy=AAAA-MM-DD]");
                return;
            } else {
                System.err.println("Argumento desconocido ignorado: " + arg);
            }
        }

        RelojSimulado reloj = RelojSimulado.delSistema();
        if (hoySimulado != null) {
            reloj.ajustarA(hoySimulado.atTime(8, 0));
        }
        Consola consola = new Consola();
        int codigoSalida = 0;
        try {
            consola.titulo("VetGest - PatasSanas | Prototipo AP3 (Java + MySQL)");
            FabricaDAO fabrica = elegirFabrica(memoria, reloj, consola);
            if (demo) {
                new EscenarioDemo(fabrica, reloj, System.out).ejecutar();
            } else {
                Usuario usuario = elegirUsuario(fabrica, consola);
                Sucursal sucursal = sucursalDe(usuario, fabrica);
                new MenuPrincipal(consola, new Sesion(usuario, sucursal, fabrica, reloj)).mostrar();
                consola.println("Sesion finalizada. Hasta luego.");
            }
        } catch (EntradaFinalizadaException e) {
            consola.println("");
            consola.println("Fin de la entrada. Sesion finalizada.");
        } catch (VetGestException e) {
            consola.error("Error no recuperable [" + e.getCodigo() + "]: " + e.getMessage());
            codigoSalida = 1;
        }
        if (codigoSalida != 0) {
            System.exit(codigoSalida);
        }
    }

    /** Polimorfismo en tiempo de ejecucion: se decide que implementacion de FabricaDAO usar. */
    private static FabricaDAO elegirFabrica(boolean memoria, RelojSimulado reloj, Consola consola) {
        if (!memoria) {
            try {
                String servidor = ConexionBD.probarConexion();
                consola.ok("Conectado a " + servidor);
                return new FabricaDAOJdbc(servidor);
            } catch (SQLException | DatosException e) {      // multi-catch: comprobada y no comprobada
                consola.aviso("No se pudo conectar a MySQL: " + e.getMessage());
                consola.aviso("Se continua en MODO MEMORIA con los datos de prueba (los cambios no se guardan).");
            }
        }
        return new FabricaDAOMemoria(BaseDatosMemoria.conDatosDePrueba(reloj));
    }

    /** CU00 simplificado: la autenticacion con hash bcrypt (RNF01) se incorpora con la interfaz grafica. */
    private static Usuario elegirUsuario(FabricaDAO fabrica, Consola consola) {
        List<Usuario> usuarios = fabrica.catalogo().listarUsuariosActivos();
        consola.println("Usuarios activos:");
        for (int i = 0; i < usuarios.size(); i++) {
            consola.printf("  %d) %s%n", i + 1, usuarios.get(i));
        }
        return usuarios.get(consola.leerEntero("Seleccione su usuario: ", 1, usuarios.size()) - 1);
    }

    private static Sucursal sucursalDe(Usuario usuario, FabricaDAO fabrica) {
        for (Sucursal s : fabrica.catalogo().listarSucursales()) {
            if (s.getId() == usuario.getIdSucursal()) {
                return s;
            }
        }
        throw new EntidadNoEncontradaException("sucursal", usuario.getIdSucursal());
    }
}
