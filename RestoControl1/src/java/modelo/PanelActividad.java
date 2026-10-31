package modelo;

public class PanelActividad {

    private String descripcion;
    private String tiempo;

    public PanelActividad() {
    }

    public PanelActividad(String descripcion, String tiempo) {
        this.descripcion = descripcion;
        this.tiempo = tiempo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTiempo() {
        return tiempo;
    }

    public void setTiempo(String tiempo) {
        this.tiempo = tiempo;
    }
}
