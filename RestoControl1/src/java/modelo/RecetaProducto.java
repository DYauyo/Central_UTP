package modelo;

import java.util.ArrayList;
import java.util.List;

public class RecetaProducto {

    private Producto producto;
    private List<InsumoRequerido> insumos = new ArrayList<>();

    public RecetaProducto() {
    }

    public RecetaProducto(Producto producto) {
        this.producto = producto;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public List<InsumoRequerido> getInsumos() {
        return insumos;
    }

    public void setInsumos(List<InsumoRequerido> insumos) {
        this.insumos = insumos == null ? new ArrayList<>() : insumos;
    }

    public int getCantidadInsumos() {
        return insumos.size();
    }

    public int getInsumosBajoMinimo() {
        int total = 0;
        for (InsumoRequerido insumo : insumos) {
            if (insumo.getStockActual().compareTo(insumo.getStockMinimo()) <= 0) {
                total++;
            }
        }
        return total;
    }

    public boolean isTieneReceta() {
        return !insumos.isEmpty();
    }

    public boolean isProductoActivo() {
        return producto != null && "activo".equalsIgnoreCase(producto.getEstado());
    }

    public boolean isCategoriaActiva() {
        return producto != null && "activo".equalsIgnoreCase(producto.getEstadoCategoria());
    }

    public boolean isListaParaEstimacion() {
        return isTieneReceta() && isProductoActivo() && isCategoriaActiva();
    }

    public String getEstadoReceta() {
        if (!isProductoActivo()) {
            return "Plato inactivo";
        }
        if (!isCategoriaActiva()) {
            return "Categoria inactiva";
        }
        if (!isTieneReceta()) {
            return "Sin receta";
        }
        return "Receta lista";
    }

    public String getEstadoCss() {
        if (isListaParaEstimacion()) {
            return "success";
        }
        if (!isTieneReceta()) {
            return "warning";
        }
        return "danger";
    }
}
