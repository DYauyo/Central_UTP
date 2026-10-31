package modelo;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DisponibilidadProducto {

    private int idDisponibilidad;
    private LocalDate fecha;
    private int cantidadPlanificada;
    private int cantidadVendida;
    private String estado;
    private int idProducto;
    private int idCategoria;
    private String nombreProducto;
    private String nombreCategoria;
    private String imagen;
    private String disponibilidadProducto;
    private String estadoProducto;
    private List<InsumoRequerido> insumos = new ArrayList<>();
    private String stockEstado = "critico";
    private String stockTitulo = "Sin receta registrada";
    private String stockDescripcion = "Registra insumos para validar stock";
    private int insumosBajos;
    private int insumosCriticos;

    public int getIdDisponibilidad() {
        return idDisponibilidad;
    }

    public void setIdDisponibilidad(int idDisponibilidad) {
        this.idDisponibilidad = idDisponibilidad;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public int getCantidadPlanificada() {
        return cantidadPlanificada;
    }

    public void setCantidadPlanificada(int cantidadPlanificada) {
        this.cantidadPlanificada = cantidadPlanificada;
        recalcularStock();
    }

    public int getCantidadVendida() {
        return cantidadVendida;
    }

    public void setCantidadVendida(int cantidadVendida) {
        this.cantidadVendida = cantidadVendida;
    }

    public int getCantidadDisponible() {
        return Math.max(0, cantidadPlanificada - cantidadVendida);
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public boolean isTieneImagen() {
        return imagen != null && !imagen.trim().isEmpty();
    }

    public boolean isImagenExterna() {
        if (!isTieneImagen()) {
            return false;
        }
        String valor = imagen.trim().toLowerCase();
        return valor.startsWith("http://") || valor.startsWith("https://");
    }

    public String getImagenLocal() {
        if (!isTieneImagen()) {
            return "";
        }
        String valor = imagen.trim();
        while (valor.startsWith("/")) {
            valor = valor.substring(1);
        }
        return valor;
    }

    public String getDisponibilidadProducto() {
        return disponibilidadProducto;
    }

    public void setDisponibilidadProducto(String disponibilidadProducto) {
        this.disponibilidadProducto = disponibilidadProducto;
    }

    public String getEstadoProducto() {
        return estadoProducto;
    }

    public void setEstadoProducto(String estadoProducto) {
        this.estadoProducto = estadoProducto;
    }

    public List<InsumoRequerido> getInsumos() {
        return insumos;
    }

    public void setInsumos(List<InsumoRequerido> insumos) {
        this.insumos = insumos == null ? new ArrayList<>() : insumos;
        recalcularStock();
    }

    public String getStockEstado() {
        return stockEstado;
    }

    public String getStockTitulo() {
        return stockTitulo;
    }

    public String getStockDescripcion() {
        return stockDescripcion;
    }

    public int getInsumosBajos() {
        return insumosBajos;
    }

    public int getInsumosCriticos() {
        return insumosCriticos;
    }

    public boolean isDisponibleParaVenta() {
        return !"critico".equals(stockEstado);
    }

    public void recalcularStock() {
        insumosBajos = 0;
        insumosCriticos = 0;

        if (insumos.isEmpty()) {
            stockEstado = "critico";
            stockTitulo = "Sin receta registrada";
            stockDescripcion = "Registra insumos para validar stock";
            return;
        }

        for (InsumoRequerido insumo : insumos) {
            insumo.calcularParaPorciones(cantidadPlanificada);
            if ("critico".equals(insumo.getEstado())) {
                insumosCriticos++;
            } else if ("bajo".equals(insumo.getEstado())) {
                insumosBajos++;
            }
        }

        if (insumosCriticos > 0) {
            stockEstado = "critico";
            stockTitulo = "Stock critico";
            stockDescripcion = insumosCriticos + " insumo(s) no alcanzan";
        } else if (insumosBajos > 0) {
            stockEstado = "bajo";
            stockTitulo = "Stock bajo";
            stockDescripcion = insumosBajos + " insumo(s) con stock bajo";
        } else {
            stockEstado = "suficiente";
            stockTitulo = "Stock suficiente";
            stockDescripcion = "Todos los insumos disponibles";
        }
    }

    public String getIconoCss() {
        String texto = normalizar((nombreProducto == null ? "" : nombreProducto) + " " + (nombreCategoria == null ? "" : nombreCategoria));
        if (contiene(texto, "bebida", "jugo", "refresco", "gaseosa", "limonada", "chicha")) {
            return "bi-cup-straw";
        }
        if (contiene(texto, "postre", "torta", "pastel", "dulce", "helado")) {
            return "bi-cake2";
        }
        if (contiene(texto, "pollo", "alita", "carne", "lomo", "parrilla")) {
            return "bi-fire";
        }
        if (contiene(texto, "combo", "promo", "oferta")) {
            return "bi-tags";
        }
        return "bi-basket";
    }

    public String getIconoColorCss() {
        String texto = normalizar((nombreProducto == null ? "" : nombreProducto) + " " + (nombreCategoria == null ? "" : nombreCategoria));
        if (contiene(texto, "bebida", "jugo", "refresco", "gaseosa", "limonada", "chicha")) {
            return "icon-pink";
        }
        if (contiene(texto, "postre", "torta", "pastel", "dulce", "helado")) {
            return "icon-purple";
        }
        if (contiene(texto, "pollo", "alita", "carne", "lomo", "parrilla")) {
            return "icon-orange";
        }
        return "icon-green";
    }

    private boolean contiene(String valor, String... palabras) {
        for (String palabra : palabras) {
            if (valor.contains(palabra)) {
                return true;
            }
        }
        return false;
    }

    private String normalizar(String valor) {
        String limpio = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return limpio.toLowerCase();
    }
}
