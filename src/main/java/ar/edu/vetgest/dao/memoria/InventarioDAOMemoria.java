package ar.edu.vetgest.dao.memoria;

import ar.edu.vetgest.algoritmos.Ordenamiento;
import ar.edu.vetgest.dao.InventarioDAO;
import ar.edu.vetgest.excepcion.EntidadNoEncontradaException;
import ar.edu.vetgest.excepcion.ReglaNegocioException;
import ar.edu.vetgest.modelo.AlertaStock;
import ar.edu.vetgest.modelo.Insumo;
import ar.edu.vetgest.modelo.LoteInsumo;
import ar.edu.vetgest.modelo.MovimientoInventario;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InventarioDAOMemoria implements InventarioDAO {

    private final BaseDatosMemoria db;

    public InventarioDAOMemoria(BaseDatosMemoria db) {
        this.db = db;
    }

    @Override
    public List<Insumo> listarInsumos() {
        return Ordenamiento.mergeSort(db.insumos.aLista(), Comparator.comparing(Insumo::getNombre));
    }

    @Override
    public List<LoteInsumo> lotesConStock(int idInsumo, int idSucursal) {
        List<LoteInsumo> resultado = new ArrayList<>();
        for (LoteInsumo l : db.lotes) {
            if (l.getIdInsumo() == idInsumo && l.getIdSucursal() == idSucursal && l.getCantidadActual() > 0) {
                resultado.add(new LoteInsumo(l));
            }
        }
        return resultado;
    }

    @Override
    public List<LoteInsumo> lotesDeSucursal(int idSucursal) {
        List<LoteInsumo> resultado = new ArrayList<>();
        for (LoteInsumo l : db.lotes) {
            if (l.getIdSucursal() == idSucursal) {
                resultado.add(new LoteInsumo(l));
            }
        }
        return Ordenamiento.mergeSort(resultado, Comparator.comparing(LoteInsumo::getNombreInsumo)
                .thenComparing(LoteInsumo::getFechaVencimiento));
    }

    @Override
    public Optional<LoteInsumo> buscarLote(int idLote) {
        return db.lotes.buscar(l -> l.getIdLote() == idLote).map(LoteInsumo::new);
    }

    @Override
    public int insertarLote(LoteInsumo lote) {
        Insumo insumo = db.insumo(lote.getIdInsumo());
        db.sucursal(lote.getIdSucursal());
        boolean duplicado = db.lotes.buscar(l -> l.getIdInsumo() == lote.getIdInsumo()
                && l.getIdSucursal() == lote.getIdSucursal() && l.getNroLote().equals(lote.getNroLote())).isPresent();
        if (duplicado) {                                                           // UNIQUE(insumo, sucursal, lote)
            throw new ReglaNegocioException("El lote " + lote.getNroLote() + " ya existe en esa sucursal");
        }
        int id = ++db.secLote;
        lote.asignarId(id);
        lote.setNombreInsumo(insumo.getNombre());
        db.lotes.agregar(new LoteInsumo(lote));
        return id;
    }

    /** Atomicidad: primero se validan todos los movimientos sobre valores provisorios y recien luego se aplican. */
    @Override
    public void registrarMovimientos(List<? extends MovimientoInventario> movimientos) {
        Map<Integer, Integer> provisorio = new HashMap<>();
        for (MovimientoInventario m : movimientos) {
            LoteInsumo lote = loteGuardado(m.getIdLote());
            int actual = provisorio.getOrDefault(m.getIdLote(), lote.getCantidadActual());
            provisorio.put(m.getIdLote(), m.aplicarA(actual));      // lanza excepcion si quedaria negativo
        }
        for (MovimientoInventario m : movimientos) {
            loteGuardado(m.getIdLote()).aplicar(m);
            db.movimientos.agregar(m);
        }
    }

    @Override
    public List<AlertaStock> alertasStockMinimo() {
        LocalDate hoy = db.hoy();
        Map<String, int[]> stockPorSucursalInsumo = new LinkedHashMap<>();   // clave "sucursal-insumo"
        for (LoteInsumo l : db.lotes) {
            String clave = l.getIdSucursal() + "-" + l.getIdInsumo();
            int[] acumulado = stockPorSucursalInsumo.computeIfAbsent(clave,
                    k -> new int[]{l.getIdSucursal(), l.getIdInsumo(), 0});
            if (l.estaVigente(hoy)) {
                acumulado[2] += l.getCantidadActual();
            }
        }
        List<AlertaStock> alertas = new ArrayList<>();
        for (int[] s : stockPorSucursalInsumo.values()) {
            Insumo insumo = db.insumo(s[1]);
            if (s[2] <= insumo.getStockMinimo()) {
                alertas.add(new AlertaStock(s[0], db.sucursal(s[0]).getNombre(), s[1], insumo.getNombre(), s[2],
                        insumo.getStockMinimo()));
            }
        }
        return Ordenamiento.mergeSort(alertas, Comparator.comparing(AlertaStock::getSucursal)
                .thenComparing(AlertaStock::getInsumo));
    }

    @Override
    public Optional<AlertaStock> verificarAlerta(int idInsumo, int idSucursal) {
        for (AlertaStock a : alertasStockMinimo()) {
            if (a.getIdInsumo() == idInsumo && a.getIdSucursal() == idSucursal) {
                return Optional.of(a);
            }
        }
        return Optional.empty();
    }

    private LoteInsumo loteGuardado(int idLote) {
        return db.lotes.buscar(l -> l.getIdLote() == idLote)
                .orElseThrow(() -> new EntidadNoEncontradaException("lote", idLote));
    }
}
