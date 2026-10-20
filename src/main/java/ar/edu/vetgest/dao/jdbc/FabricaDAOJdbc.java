package ar.edu.vetgest.dao.jdbc;

import ar.edu.vetgest.dao.CatalogoDAO;
import ar.edu.vetgest.dao.FabricaDAO;
import ar.edu.vetgest.dao.HistoriaClinicaDAO;
import ar.edu.vetgest.dao.InventarioDAO;
import ar.edu.vetgest.dao.TurnoDAO;

/** Implementacion de la fabrica que persiste en MySQL mediante JDBC. */
public class FabricaDAOJdbc implements FabricaDAO {

    private final String servidor;
    private final CatalogoDAO catalogo = new CatalogoDAOJdbc();
    private final TurnoDAO turnos = new TurnoDAOJdbc();
    private final HistoriaClinicaDAO historias = new HistoriaClinicaDAOJdbc();
    private final InventarioDAO inventario = new InventarioDAOJdbc();

    public FabricaDAOJdbc(String servidor) {
        this.servidor = servidor;
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
        return "MySQL (" + servidor + ")";
    }
}
