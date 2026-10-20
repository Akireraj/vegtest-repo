package ar.edu.vetgest.ui;

import ar.edu.vetgest.dao.FabricaDAO;
import ar.edu.vetgest.modelo.Sucursal;
import ar.edu.vetgest.modelo.Usuario;
import ar.edu.vetgest.servicio.Bitacora;
import ar.edu.vetgest.servicio.HistoriaClinicaService;
import ar.edu.vetgest.servicio.InventarioService;
import ar.edu.vetgest.servicio.PacienteService;
import ar.edu.vetgest.servicio.SalaEsperaService;
import ar.edu.vetgest.servicio.TurnoService;

import java.time.Clock;

/** Contexto de trabajo: usuario, sucursal y servicios construidos sobre la fabrica de DAO elegida. */
public class Sesion {

    private final Usuario usuario;
    private final Sucursal sucursal;
    private final FabricaDAO fabrica;
    private final Clock reloj;
    private final TurnoService turnos;
    private final HistoriaClinicaService historias;
    private final InventarioService inventario;
    private final SalaEsperaService salaEspera;
    private final PacienteService pacientes;
    private final Bitacora bitacora;

    public Sesion(Usuario usuario, Sucursal sucursal, FabricaDAO fabrica, Clock reloj) {
        this.usuario = usuario;
        this.sucursal = sucursal;
        this.fabrica = fabrica;
        this.reloj = reloj;
        this.turnos = new TurnoService(fabrica.turnos(), fabrica.catalogo(), reloj);
        this.historias = new HistoriaClinicaService(fabrica.historias(), fabrica.turnos());
        this.inventario = new InventarioService(fabrica.inventario(), reloj);
        this.salaEspera = new SalaEsperaService(fabrica.turnos(), reloj);
        this.pacientes = new PacienteService(fabrica.catalogo());
        this.bitacora = new Bitacora(reloj);
    }

    public String encabezado() {
        return usuario.getUsername() + " @ " + sucursal.getNombre() + " [" + fabrica.descripcion() + "]";
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public FabricaDAO getFabrica() {
        return fabrica;
    }

    public Clock getReloj() {
        return reloj;
    }

    public TurnoService turnos() {
        return turnos;
    }

    public HistoriaClinicaService historias() {
        return historias;
    }

    public InventarioService inventario() {
        return inventario;
    }

    public SalaEsperaService salaEspera() {
        return salaEspera;
    }

    public PacienteService pacientes() {
        return pacientes;
    }

    public Bitacora bitacora() {
        return bitacora;
    }
}
