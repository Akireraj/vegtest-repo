package ar.edu.vetgest.ui;

import ar.edu.vetgest.excepcion.ReglaNegocioException;
import ar.edu.vetgest.excepcion.StockInsuficienteException;
import ar.edu.vetgest.modelo.HistoriaClinica;
import ar.edu.vetgest.modelo.Insumo;
import ar.edu.vetgest.modelo.Mascota;
import ar.edu.vetgest.modelo.Turno;
import ar.edu.vetgest.servicio.ResultadoConsumo;

import java.math.BigDecimal;
import java.util.List;

/** CU06 Registrar historia clinica, CU07 historial y CU08 consumo de insumos (rol VETERINARIO). */
public class MenuAtencion extends Menu {

    public MenuAtencion(Consola consola, Sesion sesion) {
        super("ATENCION CLINICA", consola, sesion);
    }

    @Override
    protected String[] opciones() {
        return new String[]{
                "Atender al siguiente paciente de la sala de espera",
                "Registrar atencion por numero de turno (CU06 + CU08)",
                "Consultar historial clinico de una mascota (CU07)"};
    }

    @Override
    protected void ejecutarOpcion(int opcion) {
        switch (opcion) {
            case 1 -> atender(sesion.salaEspera().llamarSiguiente(veterinarioQueAtiende()));
            case 2 -> atender(sesion.turnos().obtener(consola.leerEntero("Numero de turno: ", 1, Integer.MAX_VALUE)));
            case 3 -> historial();
            default -> throw new IllegalStateException("Opcion no contemplada: " + opcion);
        }
    }

    /** Si el usuario es veterinario atiende su propia cola; un administrador elige de quien. */
    private int veterinarioQueAtiende() {
        if (sesion.getUsuario().esVeterinario()) {
            return sesion.getUsuario().getIdVeterinario();
        }
        List<Integer> conEspera = sesion.salaEspera().veterinariosConEspera();
        if (conEspera.isEmpty()) {
            throw new ReglaNegocioException("La sala de espera esta vacia");
        }
        for (int i = 0; i < conEspera.size(); i++) {
            consola.printf("    %d) %s%n", i + 1, sesion.pacientes().veterinario(conEspera.get(i)).getNombreCompleto());
        }
        return conEspera.get(consola.leerEntero("  Veterinario/a: ", 1, conEspera.size()) - 1);
    }

    private void atender(Turno turno) {
        Mascota mascota = sesion.pacientes().mascota(turno.getIdMascota());
        consola.println("  Paciente: " + mascota + ", " + mascota.edadEnAnios(hoy()) + " anio(s)");
        consola.println("  " + turno + " - Motivo: " + turno.getMotivo());
        consola.println("  Recomendacion preventiva: " + mascota.getRecomendacionPreventiva());
        int atencionesPrevias = sesion.historias().historial(mascota.getId()).size();
        consola.println("  Atenciones previas en la red: " + atencionesPrevias);

        BigDecimal peso = consola.leerDecimalPositivo("Peso (kg): ");
        String diagnostico = consola.leerTexto("Diagnostico: ");
        String tratamiento = consola.leerTexto("Tratamiento: ");
        String observaciones = consola.leerTextoOpcional("Observaciones (opcional): ");
        HistoriaClinica historia = sesion.historias().registrar(
                new HistoriaClinica(turno.getIdTurno(), peso, diagnostico, tratamiento, observaciones));
        consola.ok("Historia clinica " + historia.getIdHistoria() + " registrada; turno " + turno.getIdTurno() + " ATENDIDO");
        registrar("Atencion del turno " + turno.getIdTurno() + " (historia " + historia.getIdHistoria() + ")");

        while (consola.confirmar("Registrar un insumo utilizado? (s/n): ")) {
            Insumo insumo = elegirInsumo();
            int cantidad = consola.leerEntero("  Cantidad (" + insumo.getUnidad() + "): ", 1, 10_000);
            try {
                ResultadoConsumo r = sesion.inventario().registrarConsumo(historia.getIdHistoria(), insumo.getId(),
                        turno.getIdSucursal(), cantidad, sesion.getUsuario().getId());
                consola.ok("Consumo de " + insumo.getNombre() + " registrado, por lote " + r.distribucion());
                if (r.generoAlerta()) {
                    consola.aviso(r.alerta().toString());
                }
                registrar("Consumo de " + cantidad + " " + insumo.getUnidad() + " de " + insumo.getNombre());
            } catch (StockInsuficienteException e) {
                // se informa y se continua con el siguiente insumo: la atencion ya quedo registrada
                consola.error(e.getMessage() + ". Informe el faltante al encargado de stock.");
                registrar("[" + e.getCodigo() + "] " + insumo.getNombre() + ": " + e.getMessage());
            }
        }
    }

    private void historial() {
        Mascota mascota = elegirMascota(pedirCliente());
        consola.println("  Historial clinico de " + mascota.getNombre() + " (todas las sucursales):");
        Formato.historial(consola, sesion.historias().historial(mascota.getId()));
    }
}
