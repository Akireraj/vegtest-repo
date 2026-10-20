package ar.edu.vetgest.dao;

import ar.edu.vetgest.modelo.EntradaHistorial;
import ar.edu.vetgest.modelo.HistoriaClinica;

import java.util.List;

/** Persistencia de la atencion clinica (RF04, RF05). */
public interface HistoriaClinicaDAO {

    /** Inserta la historia y pasa el turno a ATENDIDO en una unica transaccion. Devuelve el id generado. */
    int registrarAtencion(HistoriaClinica historia);

    boolean existeParaTurno(int idTurno);

    List<EntradaHistorial> historialDeMascota(int idMascota);
}
