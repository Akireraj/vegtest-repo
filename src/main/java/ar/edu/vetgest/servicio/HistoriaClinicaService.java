package ar.edu.vetgest.servicio;

import ar.edu.vetgest.dao.HistoriaClinicaDAO;
import ar.edu.vetgest.dao.TurnoDAO;
import ar.edu.vetgest.excepcion.EntidadNoEncontradaException;
import ar.edu.vetgest.excepcion.ReglaNegocioException;
import ar.edu.vetgest.excepcion.TransicionInvalidaException;
import ar.edu.vetgest.modelo.EntradaHistorial;
import ar.edu.vetgest.modelo.EstadoTurno;
import ar.edu.vetgest.modelo.HistoriaClinica;
import ar.edu.vetgest.modelo.Turno;

import java.util.List;

/** CU06 Registrar historia clinica (RF04) y CU07 Consultar historial (RF05). */
public class HistoriaClinicaService {

    private final HistoriaClinicaDAO historiaDAO;
    private final TurnoDAO turnoDAO;

    public HistoriaClinicaService(HistoriaClinicaDAO historiaDAO, TurnoDAO turnoDAO) {
        this.historiaDAO = historiaDAO;
        this.turnoDAO = turnoDAO;
    }

    public HistoriaClinica registrar(HistoriaClinica historia) {
        Turno turno = turnoDAO.buscarPorId(historia.getIdTurno())
                .orElseThrow(() -> new EntidadNoEncontradaException("turno", historia.getIdTurno()));
        if (!turno.getEstado().puedePasarA(EstadoTurno.ATENDIDO)) {
            throw new TransicionInvalidaException(turno.getEstado(), EstadoTurno.ATENDIDO);
        }
        if (historiaDAO.existeParaTurno(turno.getIdTurno())) {
            throw new ReglaNegocioException("El turno " + turno.getIdTurno() + " ya tiene historia clinica");
        }
        historiaDAO.registrarAtencion(historia);   // transaccion: historia + turno ATENDIDO
        return historia;
    }

    public List<EntradaHistorial> historial(int idMascota) {
        return historiaDAO.historialDeMascota(idMascota);
    }
}
