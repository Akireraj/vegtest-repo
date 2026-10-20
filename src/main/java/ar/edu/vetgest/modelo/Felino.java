package ar.edu.vetgest.modelo;

import java.time.LocalDate;

public class Felino extends Mascota {

    public Felino(Integer id, int idCliente, String nombre, String raza, char sexo, LocalDate fechaNacimiento) {
        super(id, idCliente, nombre, raza, sexo, fechaNacimiento);
    }

    @Override
    public Especie getEspecie() {
        return Especie.FELINO;
    }

    @Override
    public int getDuracionConsultaMin() {
        return 30;
    }

    @Override
    public String getRecomendacionPreventiva() {
        return "Vacuna triple felina y antirrabica anual; control de peso y funcion renal";
    }
}
