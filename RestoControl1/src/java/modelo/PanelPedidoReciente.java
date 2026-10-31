package modelo;

import java.math.BigDecimal;

public class PanelPedidoReciente {

    private int idPedido;
    private String tipoEtiqueta;
    private String estadoPedido;
    private BigDecimal total = BigDecimal.ZERO;
    private String hora;

    public String getNumeroPedido() {
        return String.format("#%05d", idPedido);
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public String getTipoEtiqueta() {
        return tipoEtiqueta;
    }

    public void setTipoEtiqueta(String tipoEtiqueta) {
        this.tipoEtiqueta = tipoEtiqueta;
    }

    public String getEstadoPedido() {
        return estadoPedido;
    }

    public void setEstadoPedido(String estadoPedido) {
        this.estadoPedido = estadoPedido;
    }

    public String getEstadoTexto() {
        if (estadoPedido == null) {
            return "Pendiente";
        }
        switch (estadoPedido) {
            case "recibido":
                return "Recibido";
            case "en preparacion":
                return "En preparacion";
            case "listo":
                return "Listo";
            case "entregado":
                return "Entregado";
            case "pagado":
                return "Pagado";
            default:
                return "Pendiente";
        }
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }
}
