package ar.edu.vetgest.modelo;

import java.time.LocalDate;

/** Los pacientes exoticos requieren mas tiempo de manejo: la consulta sugerida es de 45 minutos. */
public class Exotico extends Mascota {

    public Exotico(Integer id, int idCliente, String nombre, String raza, char sexo, LocalDate fechaNacimiento) {
        super(id, idCliente, nombre, raza, sexo, fechaNacimiento);
    }

    @Override
    public Especie getEspecie() {
        return Especie.EXOTICO;
    }

    @Override
    public int getDuracionConsultaMin() {
        return 45;
    }

    @Override
    public String getRecomendacionPreventiva() {
        return "Control con especialista en exoticos; revision dental y de dieta cada 6 meses";
    }
}
