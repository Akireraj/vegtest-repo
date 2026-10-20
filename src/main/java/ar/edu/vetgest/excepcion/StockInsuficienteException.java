package ar.edu.vetgest.excepcion;

/** El stock vigente no alcanza para cubrir un consumo (RF06). */
public class StockInsuficienteException extends ReglaNegocioException {

    private static final long serialVersionUID = 1L;

    private final int disponible;
    private final int solicitado;

    public StockInsuficienteException(int disponible, int solicitado) {
        super("Stock vigente insuficiente: disponible " + disponible + ", solicitado " + solicitado);
        this.disponible = disponible;
        this.solicitado = solicitado;
    }

    public int getDisponible() {
        return disponible;
    }

    public int getSolicitado() {
        return solicitado;
    }

    @Override
    public String getCodigo() {
        return "STOCK";
    }
}
