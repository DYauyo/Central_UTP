package modelo;

import java.text.Normalizer;

public class Categoria {

    private int idCategoria;
    private String nombreCategoria;
    private String descripcion;
    private String estado;
    private int cantidadProductos;
    private int cantidadDisponibles;
    private int cantidadInactivos;

    public Categoria() {
    }

    public Categoria(int idCategoria, String nombreCategoria, String descripcion, String estado) {
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getCantidadProductos() {
        return cantidadProductos;
    }

    public void setCantidadProductos(int cantidadProductos) {
        this.cantidadProductos = cantidadProductos;
    }

    public int getCantidadDisponibles() {
        return cantidadDisponibles;
    }

    public void setCantidadDisponibles(int cantidadDisponibles) {
        this.cantidadDisponibles = cantidadDisponibles;
    }

    public int getCantidadInactivos() {
        return cantidadInactivos;
    }

    public void setCantidadInactivos(int cantidadInactivos) {
        this.cantidadInactivos = cantidadInactivos;
    }

    public String getIconoCss() {
        String nombre = normalizar(nombreCategoria);
        if (contiene(nombre, "bebida", "jugo", "refresco", "gaseosa", "bar")) {
            return "bi-cup-straw";
        }
        if (contiene(nombre, "postre", "dulce", "torta", "pastel")) {
            return "bi-cake2";
        }
        if (contiene(nombre, "promo", "oferta", "combo")) {
            return "bi-tags";
        }
        if (contiene(nombre, "menu", "dia", "ejecutivo")) {
            return "bi-calendar2-check";
        }
        if (contiene(nombre, "entrada", "aperitivo")) {
            return "bi-egg-fried";
        }
        if (contiene(nombre, "fondo", "plato", "principal", "comida")) {
            return "bi-basket";
        }
        if (contiene(nombre, "sopa", "caldo", "cafe", "infusion")) {
            return "bi-cup-hot";
        }
        return "bi-menu-button-wide";
    }

    public String getIconoColorCss() {
        String nombre = normalizar(nombreCategoria);
        if (contiene(nombre, "bebida", "jugo", "refresco", "gaseosa", "bar")) {
            return "icon-cyan";
        }
        if (contiene(nombre, "postre", "dulce", "torta", "pastel")) {
            return "icon-pink";
        }
        if (contiene(nombre, "promo", "oferta", "combo")) {
            return "icon-purple";
        }
        if (contiene(nombre, "menu", "dia", "ejecutivo")) {
            return "icon-blue";
        }
        if (contiene(nombre, "entrada", "aperitivo")) {
            return "icon-green";
        }
        if (contiene(nombre, "fondo", "plato", "principal", "comida")) {
            return "icon-orange";
        }
        return "icon-slate";
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
        if (valor == null) {
            return "";
        }
        String limpio = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return limpio.toLowerCase();
    }
}
