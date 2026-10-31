package modelo;

public class Reserva {

    private int idReserva;
    private String cliente;
    private String telefono;
    private int cantidadPersonas;
    private String fechaReserva;
    private String horaReserva;
    private String observacion;
    private String estadoReserva;
    private int idMesa;
    private int numeroMesa;
    private int minutosParaReserva;

    public int getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(int idReserva) {
        this.idReserva = idReserva;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public int getCantidadPersonas() {
        return cantidadPersonas;
    }

    public void setCantidadPersonas(int cantidadPersonas) {
        this.cantidadPersonas = cantidadPersonas;
    }

    public String getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(String fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public String getHoraReserva() {
        return horaReserva;
    }

    public void setHoraReserva(String horaReserva) {
        this.horaReserva = horaReserva;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getEstadoReserva() {
        return estadoReserva;
    }

    public void setEstadoReserva(String estadoReserva) {
        this.estadoReserva = estadoReserva;
    }

    public int getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(int idMesa) {
        this.idMesa = idMesa;
    }

    public int getNumeroMesa() {
        return numeroMesa;
    }

    public void setNumeroMesa(int numeroMesa) {
        this.numeroMesa = numeroMesa;
    }

    public int getMinutosParaReserva() {
        return minutosParaReserva;
    }

    public void setMinutosParaReserva(int minutosParaReserva) {
        this.minutosParaReserva = minutosParaReserva;
    }

    public boolean isAsignadaMesa() {
        return idMesa > 0;
    }

    public boolean isVencida() {
        return minutosParaReserva < 0;
    }

    public boolean isUrgente() {
        return minutosParaReserva >= 0 && minutosParaReserva <= 10;
    }

    public boolean isProxima() {
        return minutosParaReserva > 10 && minutosParaReserva <= 30;
    }

    public String getEtiquetaTiempo() {
        if (minutosParaReserva < 0) {
            return "Reserva vencida";
        }
        if (minutosParaReserva == 0) {
            return "Reserva ahora";
        }
        if (minutosParaReserva < 60) {
            return "En " + minutosParaReserva + " min";
        }
        int horas = minutosParaReserva / 60;
        int minutos = minutosParaReserva % 60;
        return minutos == 0 ? "En " + horas + " h" : "En " + horas + " h " + minutos + " min";
    }
}
