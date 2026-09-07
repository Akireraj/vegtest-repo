package com.vetgest;

import com.vetgest.modelo.Insumo;
import com.vetgest.modelo.Turno;
import com.vetgest.servicio.InventarioService;
import com.vetgest.servicio.TurnoService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * VetGest (PatasSanas) - Prototipo operacional en Java + MySQL.
 *
 * Punto de entrada que ejecuta el escenario de prueba end-to-end descripto
 * en la seccion 8.1 del informe AP1:
 *   1) reserva de turno
 *   2) intento de superposicion (rechazado)
 *   3) confirmacion y atencion del turno
 *   4) consumo de un insumo
 *   5) consulta de alertas de stock minimo
 *
 * Requiere que la base de datos vetgest_db exista y tenga cargados los
 * datos de ejemplo del script sql/vetgest_db.sql (clientes, mascotas,
 * veterinarios e insumos ya precargados).
 */
public class MainApp {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final TurnoService turnoService = new TurnoService();
    private static final InventarioService inventarioService = new InventarioService();

    // Datos de ejemplo precargados por sql/vetgest_db.sql
    private static final int ID_MASCOTA_1 = 1;   // Firulais (cliente Juan Perez)
    private static final int ID_MASCOTA_2 = 2;   // Michi (cliente Ana Gomez)
    private static final int ID_VETERINARIO = 1; // Dra. Maria Rodriguez
    private static final int ID_INSUMO_VACUNA = 1; // Vacuna antirrabica (stock 6, minimo 5)

    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println(" VetGest - PatasSanas | Prototipo Turnos + Inventario");
        System.out.println("=========================================================");

        // Horario de prueba: mañana a las 10:00
        LocalDateTime horarioTurno = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);

        try {
            // ------------------------------------------------------------
            // 1) Reserva de turno (RF01)
            // ------------------------------------------------------------
            paso("1", "Reservar turno para Firulais con la Dra. Rodriguez (" + horarioTurno.format(FMT) + ")");
            Turno turno = turnoService.reservarTurno(ID_MASCOTA_1, ID_VETERINARIO, horarioTurno);
            System.out.println("   -> OK. " + turno);

            // ------------------------------------------------------------
            // 2) Intento de superposicion: mismo veterinario, mismo horario (RF02)
            // ------------------------------------------------------------
            paso("2", "Intentar reservar OTRO turno (Michi) con el MISMO veterinario y horario");
            try {
                turnoService.reservarTurno(ID_MASCOTA_2, ID_VETERINARIO, horarioTurno);
                System.out.println("   -> ERROR: el sistema debio haber rechazado esta reserva.");
            } catch (IllegalStateException e) {
                System.out.println("   -> Rechazado correctamente (RF02): " + e.getMessage());
            }

            // ------------------------------------------------------------
            // 3) Confirmacion y atencion del turno (RF03, RF04)
            // ------------------------------------------------------------
            paso("3a", "Confirmar el turno #" + turno.getIdTurno());
            turnoService.confirmarTurno(turno.getIdTurno());
            System.out.println("   -> OK. Turno confirmado.");

            paso("3b", "Atender el turno y registrar la historia clinica");
            int idHistoria = turnoService.atenderTurno(turno.getIdTurno(),
                    "Control de rutina, sin hallazgos relevantes.",
                    "Se indica continuar plan de vacunacion segun calendario.");
            System.out.println("   -> OK. Turno marcado como ATENDIDO. Historia clinica generada #" + idHistoria);

            // ------------------------------------------------------------
            // 4) Consumo de un insumo asociado a la atencion (RF06, RF08)
            // ------------------------------------------------------------
            paso("4", "Registrar consumo de 2 dosis de 'Vacuna antirrabica' para esta atencion");
            boolean alerta = inventarioService.registrarConsumo(ID_INSUMO_VACUNA, idHistoria, 2);
            System.out.println("   -> OK. Consumo registrado."
                    + (alerta ? "  [ALERTA] El stock quedo en el minimo o por debajo (RF08)." : ""));

            // ------------------------------------------------------------
            // 5) Consulta de alertas de stock minimo (RF08)
            // ------------------------------------------------------------
            paso("5", "Consultar insumos en alerta de stock minimo");
            List<Insumo> alertas = inventarioService.consultarAlertasStockMinimo();
            if (alertas.isEmpty()) {
                System.out.println("   -> No hay insumos por debajo del stock minimo.");
            } else {
                alertas.forEach(i -> System.out.println("   -> " + i));
            }

            System.out.println();
            System.out.println("Escenario de prueba finalizado correctamente.");

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("[Error de negocio] " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("[Error de base de datos] " + e.getMessage());
            System.out.println("Verifique que vetgest_db exista y que src/main/resources/config.properties "
                    + "tenga las credenciales correctas.");
        }
    }

    private static void paso(String numero, String descripcion) {
        System.out.println();
        System.out.println("[Paso " + numero + "] " + descripcion);
    }
}
