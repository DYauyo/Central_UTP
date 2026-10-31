package modelo;

import java.math.BigDecimal;
import java.text.Normalizer;

public class Producto {

    private int idProducto;
    private String nombreProducto;
    private String descripcion;
    private BigDecimal precio;
    private String disponibilidad;
    private String imagen;
    private String estado;
    private int idCategoria;
    private String nombreCategoria;
    private String estadoCategoria;
    private int cantidadDisponible;

    public Producto() {
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(String disponibilidad) {
        this.disponibilidad = disponibilidad;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
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

    public String getEstadoCategoria() {
        return estadoCategoria;
    }

    public void setEstadoCategoria(String estadoCategoria) {
        this.estadoCategoria = estadoCategoria;
    }

    public int getCantidadDisponible() {
        return cantidadDisponible;
    }

    public void setCantidadDisponible(int cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible;
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
        if (contiene(texto, "sopa", "caldo", "cafe", "infusion")) {
            return "bi-cup-hot";
        }
        if (contiene(texto, "combo", "promo", "oferta")) {
            return "bi-tags";
        }
        return "bi-basket";
    }

    public String getIconoColorCss() {
        String texto = normalizar((nombreProducto == null ? "" : nombreProducto) + " " + (nombreCategoria == null ? "" : nombreCategoria));
        if (contiene(texto, "bebida", "jugo", "refresco", "gaseosa", "limonada", "chicha")) {
            return "icon-cyan";
        }
        if (contiene(texto, "postre", "torta", "pastel", "dulce", "helado")) {
            return "icon-pink";
        }
        if (contiene(texto, "pollo", "alita", "carne", "lomo", "parrilla")) {
            return "icon-orange";
        }
        if (contiene(texto, "combo", "promo", "oferta")) {
            return "icon-purple";
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
