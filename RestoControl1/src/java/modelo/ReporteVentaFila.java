package modelo;

import java.math.BigDecimal;

public class ReporteVentaFila {

    private String fechaHora;
    private int idPago;
    private int idPedido;
    private int numeroMesa;
    private String cliente;
    private String medioPago;
    private BigDecimal total = BigDecimal.ZERO;

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public String getTicket() {
        return "T-" + String.format("%04d", idPago);
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getNumeroMesa() {
        return numeroMesa;
    }

    public void setNumeroMesa(int numeroMesa) {
        this.numeroMesa = numeroMesa;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getMedioPago() {
        return medioPago;
    }

    public void setMedioPago(String medioPago) {
        this.medioPago = medioPago;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getEtiquetaMesa() {
        return numeroMesa > 0 ? "Mesa " + numeroMesa : "Sin mesa";
    }

    public String getEtiquetaMedio() {
        if ("tarjeta".equalsIgnoreCase(medioPago)) {
            return "Tarjeta";
        }
        if ("yape_plin".equalsIgnoreCase(medioPago)) {
            return "Yape / Plin";
        }
        return "Efectivo";
    }
}
