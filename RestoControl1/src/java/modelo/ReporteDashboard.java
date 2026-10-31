package modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ReporteDashboard {

    private String tipo = "ventas";
    private String desde;
    private String hasta;
    private String horaDesde = "00:00";
    private String horaHasta = "23:59";
    private BigDecimal ventasBrutas = BigDecimal.ZERO;
    private int totalTickets;
    private BigDecimal ticketPromedio = BigDecimal.ZERO;
    private BigDecimal ventasNetas = BigDecimal.ZERO;
    private BigDecimal utilidadBruta = BigDecimal.ZERO;
    private BigDecimal utilidadNeta = BigDecimal.ZERO;
    private BigDecimal margenNeto = BigDecimal.ZERO;
    private BigDecimal porcentajeUtilidadBruta = BigDecimal.ZERO;
    private List<ReporteMetodoPago> metodosPago = new ArrayList<>();
    private List<ReporteHoraPunta> horasPunta = new ArrayList<>();
    private List<ReporteVentaFila> ventas = new ArrayList<>();
    private List<ReporteProductoVendido> productosTop = new ArrayList<>();
    private List<ReporteCostoDistribucion> distribucionCostos = new ArrayList<>();
    private List<ReporteRentabilidadDia> rentabilidadDiaria = new ArrayList<>();

    public boolean isTipoVentas() {
        return !"rentabilidad".equalsIgnoreCase(tipo);
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDesde() {
        return desde;
    }

    public void setDesde(String desde) {
        this.desde = desde;
    }

    public String getHasta() {
        return hasta;
    }

    public void setHasta(String hasta) {
        this.hasta = hasta;
    }

    public String getHoraDesde() {
        return horaDesde;
    }

    public void setHoraDesde(String horaDesde) {
        this.horaDesde = horaDesde;
    }

    public String getHoraHasta() {
        return horaHasta;
    }

    public void setHoraHasta(String horaHasta) {
        this.horaHasta = horaHasta;
    }

    public BigDecimal getVentasBrutas() {
        return ventasBrutas;
    }

    public void setVentasBrutas(BigDecimal ventasBrutas) {
        this.ventasBrutas = ventasBrutas;
    }

    public int getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(int totalTickets) {
        this.totalTickets = totalTickets;
    }

    public BigDecimal getTicketPromedio() {
        return ticketPromedio;
    }

    public void setTicketPromedio(BigDecimal ticketPromedio) {
        this.ticketPromedio = ticketPromedio;
    }

    public BigDecimal getVentasNetas() {
        return ventasNetas;
    }

    public void setVentasNetas(BigDecimal ventasNetas) {
        this.ventasNetas = ventasNetas;
    }

    public BigDecimal getUtilidadBruta() {
        return utilidadBruta;
    }

    public void setUtilidadBruta(BigDecimal utilidadBruta) {
        this.utilidadBruta = utilidadBruta;
    }

    public BigDecimal getUtilidadNeta() {
        return utilidadNeta;
    }

    public void setUtilidadNeta(BigDecimal utilidadNeta) {
        this.utilidadNeta = utilidadNeta;
    }

    public BigDecimal getMargenNeto() {
        return margenNeto;
    }

    public void setMargenNeto(BigDecimal margenNeto) {
        this.margenNeto = margenNeto;
    }

    public BigDecimal getPorcentajeUtilidadBruta() {
        return porcentajeUtilidadBruta;
    }

    public void setPorcentajeUtilidadBruta(BigDecimal porcentajeUtilidadBruta) {
        this.porcentajeUtilidadBruta = porcentajeUtilidadBruta;
    }

    public List<ReporteMetodoPago> getMetodosPago() {
        return metodosPago;
    }

    public void setMetodosPago(List<ReporteMetodoPago> metodosPago) {
        this.metodosPago = metodosPago;
    }

    public List<ReporteHoraPunta> getHorasPunta() {
        return horasPunta;
    }

    public void setHorasPunta(List<ReporteHoraPunta> horasPunta) {
        this.horasPunta = horasPunta;
    }

    public List<ReporteVentaFila> getFilasVentas() {
        return ventas;
    }

    public void setFilasVentas(List<ReporteVentaFila> ventas) {
        this.ventas = ventas;
    }

    public List<ReporteCostoDistribucion> getDistribucionCostos() {
        return distribucionCostos;
    }

    public void setDistribucionCostos(List<ReporteCostoDistribucion> distribucionCostos) {
        this.distribucionCostos = distribucionCostos;
    }

    public List<ReporteProductoVendido> getProductosTop() {
        return productosTop;
    }

    public void setProductosTop(List<ReporteProductoVendido> productosTop) {
        this.productosTop = productosTop;
    }

    public List<ReporteRentabilidadDia> getRentabilidadDiaria() {
        return rentabilidadDiaria;
    }

    public void setRentabilidadDiaria(List<ReporteRentabilidadDia> rentabilidadDiaria) {
        this.rentabilidadDiaria = rentabilidadDiaria;
    }
}
