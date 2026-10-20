package ar.edu.vetgest.modelo;

import java.time.LocalDate;

public class Canino extends Mascota {

    public Canino(Integer id, int idCliente, String nombre, String raza, char sexo, LocalDate fechaNacimiento) {
        super(id, idCliente, nombre, raza, sexo, fechaNacimiento);
    }

    @Override
    public Especie getEspecie() {
        return Especie.CANINO;
    }

    @Override
    public int getDuracionConsultaMin() {
        return 30;
    }

    @Override
    public String getRecomendacionPreventiva() {
        return "Vacuna sextuple y antirrabica anual; desparasitacion cada 3 meses";
    }
}
