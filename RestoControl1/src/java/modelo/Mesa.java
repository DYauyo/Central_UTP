package modelo;

import java.math.BigDecimal;

public class Mesa {

    private int idMesa;
    private int numeroMesa;
    private int capacidad;
    private String estadoMesa;
    private String ubicacion;
    private String estado;
    private int idPedidoAbierto;
    private BigDecimal totalPedido;
    private String cliente;
    private String mozo;
    private String tiempoAbierto;
    private String mesasUnidas;
    private String estadoPedido;
    private boolean mesaPrincipalUnion;
    private Reserva reservaProxima;

    public Mesa() {
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

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public String getEstadoMesa() {
        return estadoMesa;
    }

    public void setEstadoMesa(String estadoMesa) {
        this.estadoMesa = estadoMesa;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getIdPedidoAbierto() {
        return idPedidoAbierto;
    }

    public void setIdPedidoAbierto(int idPedidoAbierto) {
        this.idPedidoAbierto = idPedidoAbierto;
    }

    public BigDecimal getTotalPedido() {
        return totalPedido;
    }

    public void setTotalPedido(BigDecimal totalPedido) {
        this.totalPedido = totalPedido;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getMozo() {
        return mozo;
    }

    public void setMozo(String mozo) {
        this.mozo = mozo;
    }

    public String getTiempoAbierto() {
        return tiempoAbierto;
    }

    public void setTiempoAbierto(String tiempoAbierto) {
        this.tiempoAbierto = tiempoAbierto;
    }

    public String getMesasUnidas() {
        return mesasUnidas;
    }

    public void setMesasUnidas(String mesasUnidas) {
        this.mesasUnidas = mesasUnidas;
    }

    public String getEstadoPedido() {
        return estadoPedido;
    }

    public void setEstadoPedido(String estadoPedido) {
        this.estadoPedido = estadoPedido;
    }

    public boolean isMesaPrincipalUnion() {
        return mesaPrincipalUnion;
    }

    public void setMesaPrincipalUnion(boolean mesaPrincipalUnion) {
        this.mesaPrincipalUnion = mesaPrincipalUnion;
    }

    public boolean isMesaUnida() {
        return mesasUnidas != null && !mesasUnidas.trim().isEmpty();
    }

    public Reserva getReservaProxima() {
        return reservaProxima;
    }

    public void setReservaProxima(Reserva reservaProxima) {
        this.reservaProxima = reservaProxima;
    }

    public boolean isTieneReservaProxima() {
        return reservaProxima != null;
    }

    public String getClaseReserva() {
        if (reservaProxima == null) {
            return "";
        }
        if (reservaProxima.isVencida()) {
            return "has-reservation expired";
        }
        if (reservaProxima.isUrgente()) {
            return "has-reservation urgent";
        }
        if (reservaProxima.isProxima()) {
            return "has-reservation soon";
        }
        return "has-reservation soft";
    }

    public String getClaseBadgeReserva() {
        if (reservaProxima == null) {
            return "";
        }
        if (reservaProxima.isVencida()) {
            return "expired";
        }
        if (reservaProxima.isUrgente()) {
            return "urgent";
        }
        if (reservaProxima.isProxima()) {
            return "soon";
        }
        return "soft";
    }

    public String getEtiquetaReserva() {
        if (reservaProxima == null) {
            return "";
        }
        if (reservaProxima.isVencida()) {
            return "Reserva vencida";
        }
        if (reservaProxima.isUrgente()) {
            return "Reserva urgente";
        }
        return "Reservada";
    }

    public boolean isOcupada() {
        return idPedidoAbierto > 0 || "ocupada".equalsIgnoreCase(estadoMesa);
    }

    public boolean isListaParaCobro() {
        return "listo".equalsIgnoreCase(estadoPedido) || "entregado".equalsIgnoreCase(estadoPedido);
    }

    public String getEstadoVisual() {
        if (isListaParaCobro()) {
            return "Por cobrar";
        }
        if (isMesaUnida()) {
            return "Unida";
        }
        if (isOcupada()) {
            return "Ocupada";
        }
        if (isTieneReservaProxima()) {
            return "Reservada";
        }
        return "";
    }

    public String getClaseColor() {
        if (!isOcupada()) {
            return "tile-green";
        }
        if (isListaParaCobro()) {
            return "tile-cyan";
        }
        if (isMesaUnida()) {
            return "tile-purple";
        }
        if ("recibido".equalsIgnoreCase(estadoPedido) || "en preparacion".equalsIgnoreCase(estadoPedido)) {
            return "tile-yellow";
        }
        return "tile-orange";
    }
}
