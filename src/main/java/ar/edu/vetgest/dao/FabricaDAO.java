package ar.edu.vetgest.dao;

/**
 * Fabrica abstracta de DAO. MainApp elige en tiempo de ejecucion la
 * implementacion (MySQL o memoria) y el resto del sistema solo conoce esta
 * interfaz: es el punto central del polimorfismo en la capa de datos.
 */
public interface FabricaDAO {

    CatalogoDAO catalogo();

    TurnoDAO turnos();

    HistoriaClinicaDAO historias();

    InventarioDAO inventario();

    /** Texto que se muestra en el encabezado del menu. */
    String descripcion();
}
