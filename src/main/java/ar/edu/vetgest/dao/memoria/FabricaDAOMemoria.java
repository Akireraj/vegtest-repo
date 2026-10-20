package ar.edu.vetgest.dao.memoria;

import ar.edu.vetgest.dao.CatalogoDAO;
import ar.edu.vetgest.dao.FabricaDAO;
import ar.edu.vetgest.dao.HistoriaClinicaDAO;
import ar.edu.vetgest.dao.InventarioDAO;
import ar.edu.vetgest.dao.TurnoDAO;

/** Implementacion de la fabrica sin base de datos (demostracion y pruebas). */
public class FabricaDAOMemoria implements FabricaDAO {

    private final CatalogoDAO catalogo;
    private final TurnoDAO turnos;
    private final HistoriaClinicaDAO historias;
    private final InventarioDAO inventario;

    public FabricaDAOMemoria(BaseDatosMemoria db) {
        this.catalogo = new CatalogoDAOMemoria(db);
        this.turnos = new TurnoDAOMemoria(db);
        this.historias = new HistoriaClinicaDAOMemoria(db);
        this.inventario = new InventarioDAOMemoria(db);
    }

    @Override
    public CatalogoDAO catalogo() {
        return catalogo;
    }

    @Override
    public TurnoDAO turnos() {
        return turnos;
    }

    @Override
    public HistoriaClinicaDAO historias() {
        return historias;
    }

    @Override
    public InventarioDAO inventario() {
        return inventario;
    }

    @Override
    public String descripcion() {
        return "MEMORIA (datos de prueba, sin persistencia)";
    }
}
