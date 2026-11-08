package ar.edu.vetgest.dao;

/**
 * Patron de diseno Fabrica abstracta (Abstract Factory, Gamma et al., 1994),
 * combinado con DAO (Data Access Object).
 * <p>
 * Roles: esta interfaz es la <b>fabrica abstracta</b>; FabricaDAOJdbc y
 * FabricaDAOMemoria son las <b>fabricas concretas</b>; CatalogoDAO, TurnoDAO,
 * HistoriaClinicaDAO e InventarioDAO son los <b>productos abstractos</b>, y
 * sus implementaciones Jdbc y Memoria, los <b>productos concretos</b>. El
 * <b>cliente</b> es Sesion (y EscenarioDemo), que arma los servicios con los
 * DAO que recibe sin conocer su clase concreta.
 * <p>
 * MainApp elige la familia una sola vez, al iniciar: si hay conexion a MySQL,
 * FabricaDAOJdbc; si no, FabricaDAOMemoria. Asi se garantiza que nunca se
 * mezclen DAO de familias distintas (por ejemplo, turnos en MySQL e
 * inventario en memoria).
 */
public interface FabricaDAO {

    CatalogoDAO catalogo();

    TurnoDAO turnos();

    HistoriaClinicaDAO historias();

    InventarioDAO inventario();

    /** Texto que se muestra en el encabezado del menu. */
    String descripcion();
}
