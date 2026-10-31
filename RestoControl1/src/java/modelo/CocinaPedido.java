package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CocinaPedido implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idPedido;
    private String fechaPedido;
    private String horaPedido;
    private String tipoPedido;
    private String estadoPedido;
    private String cliente;
    private int idMesa;
    private int numeroMesa;
    private String mozo;
    private int minutosEspera;
    private boolean primeroEnCola;
    private List<DetallePedido> detalles = new ArrayList<>();

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public String getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(String fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public String getHoraPedido() {
        return horaPedido;
    }

    public void setHoraPedido(String horaPedido) {
        this.horaPedido = horaPedido;
    }

    public String getTipoPedido() {
        return tipoPedido;
    }

    public void setTipoPedido(String tipoPedido) {
        this.tipoPedido = tipoPedido;
    }

    public String getEstadoPedido() {
        return estadoPedido;
    }

    public void setEstadoPedido(String estadoPedido) {
        this.estadoPedido = estadoPedido;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
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

    public String getMozo() {
        return mozo;
    }

    public void setMozo(String mozo) {
        this.mozo = mozo;
    }

    public int getMinutosEspera() {
        return minutosEspera;
    }

    public void setMinutosEspera(int minutosEspera) {
        this.minutosEspera = minutosEspera;
    }

    public boolean isPrimeroEnCola() {
        return primeroEnCola;
    }

    public void setPrimeroEnCola(boolean primeroEnCola) {
        this.primeroEnCola = primeroEnCola;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles == null ? new ArrayList<>() : detalles;
    }

    public int getTotalItems() {
        int total = 0;
        for (DetallePedido detalle : detalles) {
            total += detalle.getCantidad();
        }
        return total;
    }

    public boolean isRetrasado() {
        return minutosEspera >= 25 && ("recibido".equals(estadoPedido) || "en preparacion".equals(estadoPedido));
    }

    public String getTiempoTexto() {
        if (minutosEspera <= 0) {
            return "Ahora";
        }
        if (minutosEspera < 60) {
            return minutosEspera + " min";
        }
        int horas = minutosEspera / 60;
        int minutos = minutosEspera % 60;
        if (minutos == 0) {
            return horas + " h";
        }
        return horas + " h " + minutos + " min";
    }

    public String getUbicacionTexto() {
        if (numeroMesa > 0) {
            return "Mesa " + numeroMesa;
        }
        if (tipoPedido != null && tipoPedido.toLowerCase().contains("llevar")) {
            return "Para llevar";
        }
        if (tipoPedido != null && tipoPedido.toLowerCase().contains("delivery")) {
            return "Delivery";
        }
        return "Pedido rapido";
    }

    public String getClienteTexto() {
        if (cliente != null && !cliente.trim().isEmpty()) {
            return cliente;
        }
        if (mozo != null && !mozo.trim().isEmpty()) {
            return mozo;
        }
        return "Cliente";
    }
}
