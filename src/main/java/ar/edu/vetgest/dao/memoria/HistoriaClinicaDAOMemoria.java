package ar.edu.vetgest.dao.memoria;

import ar.edu.vetgest.algoritmos.Ordenamiento;
import ar.edu.vetgest.dao.HistoriaClinicaDAO;
import ar.edu.vetgest.excepcion.ReglaNegocioException;
import ar.edu.vetgest.modelo.EntradaHistorial;
import ar.edu.vetgest.modelo.EstadoTurno;
import ar.edu.vetgest.modelo.HistoriaClinica;
import ar.edu.vetgest.modelo.Turno;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HistoriaClinicaDAOMemoria implements HistoriaClinicaDAO {

    private final BaseDatosMemoria db;

    public HistoriaClinicaDAOMemoria(BaseDatosMemoria db) {
        this.db = db;
    }

    @Override
    public int registrarAtencion(HistoriaClinica historia) {
        Turno turno = db.turno(historia.getIdTurno());
        if (existeParaTurno(historia.getIdTurno())) {                              // UNIQUE(id_turno)
            throw new ReglaNegocioException("El turno " + historia.getIdTurno() + " ya tiene historia clinica");
        }
        if (turno.getEstado() != EstadoTurno.CONFIRMADO) {
            throw new ReglaNegocioException("El turno " + historia.getIdTurno() + " ya no esta CONFIRMADO");
        }
        int id = ++db.secHistoria;
        historia.asignarId(id);
        db.historias.agregar(historia);
        turno.cambiarEstado(EstadoTurno.ATENDIDO);
        return id;
    }

    @Override
    public boolean existeParaTurno(int idTurno) {
        return db.historias.buscar(h -> h.getIdTurno() == idTurno).isPresent();
    }

    @Override
    public List<EntradaHistorial> historialDeMascota(int idMascota) {
        List<EntradaHistorial> historial = new ArrayList<>();
        for (HistoriaClinica h : db.historias) {
            Turno t = db.turno(h.getIdTurno());
            if (t.getIdMascota() == idMascota) {
                historial.add(new EntradaHistorial(t.getFechaHora().toLocalDate(),
                        db.sucursal(t.getIdSucursal()).getNombre(),
                        db.veterinario(t.getIdVeterinario()).getNombreCompleto(),
                        h.getPesoKg(), h.getDiagnostico(), h.getTratamiento()));
            }
        }
        return Ordenamiento.mergeSort(historial, Comparator.comparing(EntradaHistorial::fecha).reversed());
    }
}
